<template>
  <div class="analytics-page">

    <div class="page-header">
      <h1 class="page-title">Analytics</h1>
      <p class="page-subtitle">Powered by historical data from 16,000+ games</p>
    </div>

    <div v-if="rateLimited" class="rate-limit-banner">
      ⚠️ Rate limit reached. Please wait a moment before running more analyses.
    </div>

    <div class="sections">

      <section class="card section">
        <div class="section-header">
          <div>
            <h2 class="section-title">Game Success Score</h2>
            <p class="section-desc">Weighted score based on critic (70%) and user (30%) reviews.</p>
          </div>
        </div>

        <div class="search-game-wrap">
          <div class="search-game-input-wrap">
            <input
                v-model="gameSearch"
                class="input"
                type="text"
                placeholder="Search game by title..."
                @input="showDropdown = true"
                @blur="hideDropdownSoon"
            />
            <div v-if="showDropdown && gameSearch && filteredGames.length" class="game-dropdown">
              <button
                  v-for="g in filteredGames.slice(0, 8)"
                  :key="g.id"
                  class="dropdown-item"
                  @mousedown.prevent="selectGame(g)"
              >
                <span class="dropdown-title">{{ g.title }}</span>
                <span class="dropdown-meta">{{ g.platformName }} · {{ g.genre }}</span>
              </button>
            </div>
          </div>
          <button
              class="btn-primary"
              :disabled="!selectedGame || loadingScore"
              @click="runSuccessScore"
          >
            {{ loadingScore ? 'Calculating...' : 'Calculate Score' }}
          </button>
        </div>

        <div v-if="selectedGame" class="selected-game-tag">
          Selected: <strong>{{ selectedGame.title }}</strong>
          <button @click="clearGame" class="clear-btn">✕</button>
        </div>

        <div v-if="scoreResult !== null" class="score-result">
          <div class="score-display" :class="successScoreClass">
            {{ scoreResult.toFixed(1) }}
          </div>
          <div class="score-info">
            <span class="score-label-big">Success Score</span>
            <span class="score-hint">{{ successScoreLabel }}</span>
          </div>
          <div class="score-bar-wrap">
            <div class="score-bar-track">
              <div class="score-bar-fill" :class="successScoreClass" :style="{ width: scoreResult + '%' }"></div>
            </div>
            <span class="score-bar-max">100</span>
          </div>
        </div>

        <div v-if="scoreError" class="error-msg">{{ scoreError }}</div>
      </section>

      <section class="card section">
        <div class="section-header">
          <div>
            <h2 class="section-title">Genre Analytics</h2>
            <p class="section-desc">Market insights based on historical sales data for a selected genre.</p>
          </div>
        </div>

        <div class="genre-select-wrap">
          <select v-model="selectedGenre" class="input">
            <option value="">Select genre...</option>
            <option v-for="g in genres" :key="g" :value="g">{{ g }}</option>
          </select>
        </div>

        <div class="analytics-cards">

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🔮</span>
              <div>
                <div class="analytics-card-title">Revenue Predictor</div>
                <div class="analytics-card-desc">
                  Shows the average ratio of EU sales to NA sales across all {{ selectedGenre || 'selected genre' }} games.
                  A ratio above 1 means EU tends to outperform NA for this genre.
                </div>
              </div>
            </div>
            <button
                class="btn-run"
                :disabled="!selectedGenre || loadingMap.revenue"
                @click="runAnalytic('revenue')"
            >
              {{ loadingMap.revenue ? 'Running...' : 'Run' }}
            </button>
            <div v-if="results.revenue !== null" class="analytic-result">
              <span class="result-value">{{ results.revenue.toFixed(3) }}x</span>
              <span class="result-hint">EU revenue ≈ {{ (results.revenue * 100).toFixed(1) }}% of NA revenue</span>
            </div>
            <div v-if="errors.revenue" class="error-msg">{{ errors.revenue }}</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🌍</span>
              <div>
                <div class="analytics-card-title">Global Adaptability</div>
                <div class="analytics-card-desc">
                  Measures how evenly {{ selectedGenre || 'this genre' }} games sell across NA, EU and JP.
                  A high score means the genre has broad international appeal rather than being dominant in just one region.
                </div>
              </div>
            </div>
            <button
                class="btn-run"
                :disabled="!selectedGenre || loadingMap.adaptability"
                @click="runAnalytic('adaptability')"
            >
              {{ loadingMap.adaptability ? 'Running...' : 'Run' }}
            </button>
            <div v-if="results.adaptability !== null" class="analytic-result">
              <div class="progress-wrap">
                <div class="progress-track">
                  <div
                      class="progress-fill"
                      :class="adaptabilityClass"
                      :style="{ width: results.adaptability + '%' }"
                  ></div>
                </div>
                <span class="progress-value">{{ results.adaptability.toFixed(1) }} / 100</span>
              </div>
              <span class="result-hint">{{ adaptabilityLabel }}</span>
            </div>
            <div v-if="errors.adaptability" class="error-msg">{{ errors.adaptability }}</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">📍</span>
              <div>
                <div class="analytics-card-title">Region Priority</div>
                <div class="analytics-card-desc">
                  The region where {{ selectedGenre || 'this genre' }} games sell the most on average per title.
                  Indicates where the audience for this genre is the largest and most active.
                </div>
              </div>
            </div>
            <button
                class="btn-run"
                :disabled="!selectedGenre || loadingMap.region"
                @click="runAnalytic('region')"
            >
              {{ loadingMap.region ? 'Running...' : 'Run' }}
            </button>
            <div v-if="results.region !== null" class="analytic-result">
              <span class="region-badge" :class="regionClass">{{ results.region }}</span>
              <span class="result-hint">{{ selectedGenre }} games sell most in this region on average</span>
            </div>
            <div v-if="errors.region" class="error-msg">{{ errors.region }}</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">⚠️</span>
              <div>
                <div class="analytics-card-title">Investment Risk</div>
                <div class="analytics-card-desc">
                  Percentage of {{ selectedGenre || 'this genre' }} games that received strong critic reviews (score > 70)
                  but still sold below the genre average. High percentage signals that critical success alone doesn't guarantee sales.
                </div>
              </div>
            </div>
            <button
                class="btn-run"
                :disabled="!selectedGenre || loadingMap.risk"
                @click="runAnalytic('risk')"
            >
              {{ loadingMap.risk ? 'Running...' : 'Run' }}
            </button>
            <div v-if="results.risk !== null" class="analytic-result">
              <span class="result-value" :class="riskClass">{{ results.risk.toFixed(1) }}%</span>
              <span class="result-hint">{{ riskLabel }}</span>
            </div>
            <div v-if="errors.risk" class="error-msg">{{ errors.risk }}</div>
          </div>

        </div>

      </section>

      <section class="card section">
        <div class="section-header">
          <div>
            <h2 class="section-title">Sales Race</h2>
            <p class="section-desc">Top 10 by total global sales — visualized as a race.</p>
          </div>
        </div>

        <div class="race-controls">
          <div class="race-toggle">
            <button class="race-toggle-btn" :class="{ 'race-toggle-active': raceType === 'publisher' }" @click="raceType = 'publisher'">By Publisher</button>
            <button class="race-toggle-btn" :class="{ 'race-toggle-active': raceType === 'genre' }" @click="raceType = 'genre'">By Genre</button>
          </div>
          <button class="btn-run" :disabled="!allGames.length || raceRunning" @click="runSalesRace">
            {{ raceRunning ? 'Loading...' : raceItems.length ? 'Rerun' : 'Run' }}
          </button>
        </div>

        <div v-if="raceItems.length" class="race-chart">
          <div v-for="(item, i) in raceItems" :key="item.name" class="race-item">
            <span class="race-rank">#{{ i + 1 }}</span>
            <span class="race-name">{{ item.name }}</span>
            <div class="race-track">
              <div class="race-fill" :style="{ width: item.width + '%', transition: 'width 0.5s ease' }"></div>
            </div>
            <span class="race-value">{{ item.total }}M</span>
          </div>
        </div>
        <p v-else class="race-empty">Select a type and click Run.</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'

const route = useRoute()

const allGames = ref([])
const genres = ref([])

const gameSearch = ref('')
const selectedGame = ref(null)
const showDropdown = ref(false)
const loadingScore = ref(false)
const scoreResult = ref(null)
const scoreError = ref('')

const selectedGenre = ref('')
const loadingMap = ref({ revenue: false, adaptability: false, region: false, risk: false })
const results = ref({ revenue: null, adaptability: null, region: null, risk: null })
const errors = ref({ revenue: '', adaptability: '', region: '', risk: '' })
const rateLimited = ref(false)

onMounted(async () => {
  const [gamesRes, genresRes] = await Promise.all([
    api.get('/games'),
    api.get('/games/genres'),
  ])
  allGames.value = gamesRes.data.data ?? []
  genres.value = genresRes.data ?? []

  if (route.query.genre) {
    selectedGenre.value = route.query.genre
  }
  if (route.query.gameId) {
    const found = allGames.value.find(g => g.id == route.query.gameId)
    if (found) selectGame(found)
  }
})

watch(selectedGenre, () => {
  results.value = { revenue: null, adaptability: null, region: null, risk: null }
  errors.value = { revenue: '', adaptability: '', region: '', risk: '' }
})

const filteredGames = computed(() => {
  const q = gameSearch.value.toLowerCase()
  if (!q) return []
  return allGames.value.filter(g => g.title?.toLowerCase().includes(q))
})

function selectGame(g) {
  selectedGame.value = g
  gameSearch.value = g.title
  showDropdown.value = false
  scoreResult.value = null
  scoreError.value = ''
}

function clearGame() {
  selectedGame.value = null
  gameSearch.value = ''
  scoreResult.value = null
  scoreError.value = ''
}

function hideDropdownSoon() {
  setTimeout(() => { showDropdown.value = false }, 150)
}

async function runSuccessScore() {
  if (!selectedGame.value) return
  loadingScore.value = true
  scoreError.value = ''
  scoreResult.value = null
  try {
    const res = await api.get('/analytics/success-score', { params: { gameId: selectedGame.value.id } })
    scoreResult.value = res.data.value
  } catch (e) {
    if (e.response?.status === 429) {
      rateLimited.value = true
      scoreError.value = 'Rate limit reached.'
    } else if (e.response?.status === 404) {
      scoreError.value = 'No review data available for this game.'
    } else {
      scoreError.value = 'Something went wrong.'
    }
  } finally {
    loadingScore.value = false
  }
}

const endpointMap = {
  revenue: '/analytics/revenue-predictor',
  adaptability: '/analytics/global-adaptability',
  region: '/analytics/region-priority',
  risk: '/analytics/investment-risk',
}

async function runAnalytic(key) {
  if (!selectedGenre.value) return
  loadingMap.value[key] = true
  errors.value[key] = ''
  results.value[key] = null
  rateLimited.value = false
  try {
    const res = await api.get(endpointMap[key], { params: { genre: selectedGenre.value } })
    if (key === 'region') {
      results.value[key] = res.data.label ?? res.data.value ?? null
    } else {
      results.value[key] = res.data.value ?? null
    }
  } catch (e) {
    if (e.response?.status === 429) {
      rateLimited.value = true
      errors.value[key] = 'Rate limit reached.'
    } else if (e.response?.status === 404) {
      errors.value[key] = 'No data for this genre.'
    } else {
      errors.value[key] = 'Something went wrong.'
    }
  } finally {
    loadingMap.value[key] = false
  }
}

const successScoreClass = computed(() => {
  if (scoreResult.value === null) return ''
  if (scoreResult.value >= 75) return 'score-good'
  if (scoreResult.value >= 50) return 'score-ok'
  return 'score-bad'
})

const successScoreLabel = computed(() => {
  if (scoreResult.value === null) return ''
  if (scoreResult.value >= 75) return 'Well received by critics and players'
  if (scoreResult.value >= 50) return 'Mixed reception'
  return 'Poor reception overall'
})

const adaptabilityClass = computed(() => {
  const v = results.value.adaptability
  if (v === null) return ''
  if (v >= 70) return 'prog-good'
  if (v >= 40) return 'prog-ok'
  return 'prog-bad'
})

const adaptabilityLabel = computed(() => {
  const v = results.value.adaptability
  if (v === null) return ''
  if (v >= 70) return 'Sells evenly across all regions'
  if (v >= 40) return 'Moderate regional spread'
  return 'Highly concentrated in one region'
})

const regionClass = computed(() => {
  const r = results.value.region
  if (r === 'NA') return 'region-na'
  if (r === 'EU') return 'region-eu'
  if (r === 'JP') return 'region-jp'
  return ''
})

const riskClass = computed(() => {
  const v = results.value.risk
  if (v === null) return ''
  if (v >= 60) return 'score-bad'
  if (v >= 35) return 'score-ok'
  return 'score-good'
})

const riskLabel = computed(() => {
  const v = results.value.risk
  if (v === null) return ''
  if (v >= 60) return 'High risk — many well-reviewed games underperformed'
  if (v >= 35) return 'Moderate risk'
  return 'Low risk — good reviews tend to correlate with sales'
})

const raceType = ref('publisher')
const raceItems = ref([])
const raceRunning = ref(false)

function runSalesRace() {
  raceItems.value = []
  raceRunning.value = true

  const map = {}
  for (const g of allGames.value) {
    const key = raceType.value === 'publisher' ? g.publisher : g.genre
    if (!key) continue
    const sales = g.globalSales ?? ((g.naSales ?? 0) + (g.euSales ?? 0) + (g.jpSales ?? 0))
    map[key] = (map[key] ?? 0) + sales
  }

  const sorted = Object.entries(map)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 10)
      .map(([name, total]) => ({ name, total: +total.toFixed(1) }))

  const max = sorted[0]?.total ?? 1

  sorted.forEach((item, i) => {
    setTimeout(() => {
      const idx = raceItems.value.push({ name: item.name, total: item.total, width: 0 }) - 1
      setTimeout(() => {
        raceItems.value[idx].width = (item.total / max) * 100
      }, 50)
      if (i === sorted.length - 1) raceRunning.value = false
    }, i * 140)
  })
}
</script>

<style scoped>
.analytics-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.page-title {
  font-size: 1.75rem;
  margin: 0;
}

.page-subtitle {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
}

.rate-limit-banner {
  padding: 0.875rem 1.25rem;
  border-radius: 10px;
  background-color: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  color: #fcd34d;
  font-size: 0.875rem;
}

.sections {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.75rem;
}

.section-header {
  margin-bottom: 1.5rem;
}

.section-title {
  font-size: 1.1rem;
  margin: 0 0 0.35rem;
  color: var(--color-text);
}

.section-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
}

.search-game-wrap {
  display: flex;
  gap: 0.75rem;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.search-game-input-wrap {
  position: relative;
  flex: 1;
}

.game-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  z-index: 50;
  max-height: 220px;
  overflow-y: auto;
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
}

.dropdown-item {
  width: 100%;
  padding: 0.5rem 0.75rem;
  background: none;
  border: none;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.15rem;
  transition: background-color 0.1s;
}

.dropdown-item:hover { background-color: var(--color-surface-2); }

.dropdown-title {
  font-size: 0.875rem;
  color: var(--color-text);
  font-weight: 500;
}

.dropdown-meta {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.selected-game-tag {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.clear-btn {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 0.8rem;
  padding: 0;
  line-height: 1;
}

.clear-btn:hover { color: var(--color-text); }

.score-result {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  flex-wrap: wrap;
  padding: 1rem;
  background-color: var(--color-surface-2);
  border-radius: 10px;
  border: 1px solid var(--color-border);
}

.score-display {
  font-family: 'Space Mono', monospace;
  font-size: 2.5rem;
  font-weight: 700;
  min-width: 80px;
  text-align: center;
}

.score-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.score-label-big {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.score-hint {
  font-size: 0.775rem;
  color: var(--color-text-muted);
}

.score-bar-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 120px;
}

.score-bar-track {
  flex: 1;
  height: 8px;
  background-color: var(--color-border);
  border-radius: 999px;
  overflow: hidden;
}

.score-bar-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.6s ease;
}

.score-bar-max {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.score-good { color: #86efac; }
.score-ok   { color: #fcd34d; }
.score-bad  { color: #fca5a5; }

.score-bar-fill.score-good { background-color: #22c55e; }
.score-bar-fill.score-ok   { background-color: #f59e0b; }
.score-bar-fill.score-bad  { background-color: #ef4444; }

.genre-select-wrap {
  margin-bottom: 1.5rem;
}

.analytics-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.analytics-card {
  background-color: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
}

.analytics-card-header {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.analytics-icon {
  font-size: 1.5rem;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.analytics-card-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 0.25rem;
}

.analytics-card-desc {
  font-size: 0.775rem;
  color: var(--color-text-muted);
  line-height: 1.4;
}

.btn-run {
  padding: 0.4rem 1rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  align-self: flex-start;
  transition: background-color 0.15s;
}

.btn-run:hover:not(:disabled) { background-color: var(--color-accent-hover); }
.btn-run:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-primary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  white-space: nowrap;
  transition: background-color 0.15s;
}

.btn-primary:hover:not(:disabled) { background-color: var(--color-accent-hover); }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }

.analytic-result {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border);
}

.result-value {
  font-family: 'Space Mono', monospace;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text);
}

.result-hint {
  font-size: 0.775rem;
  color: var(--color-text-muted);
}

.progress-wrap {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.progress-track {
  flex: 1;
  height: 8px;
  background-color: var(--color-border);
  border-radius: 999px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.6s ease;
}

.prog-good { background-color: #22c55e; }
.prog-ok   { background-color: #f59e0b; }
.prog-bad  { background-color: #ef4444; }

.progress-value {
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  color: var(--color-text);
  white-space: nowrap;
}

.region-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 12px;
  font-family: 'Space Mono', monospace;
  font-size: 1.1rem;
  font-weight: 700;
}

.region-na { background-color: rgba(108, 99, 255, 0.2); color: #a5b4fc; }
.region-eu { background-color: rgba(34, 197, 94, 0.2);  color: #86efac; }
.region-jp { background-color: rgba(245, 158, 11, 0.2); color: #fcd34d; }

.error-msg {
  font-size: 0.8rem;
  color: #fca5a5;
  padding: 0.5rem 0.75rem;
  background-color: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
}

.race-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.race-toggle {
  display: flex;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
}

.race-toggle-btn {
  padding: 0.4rem 0.875rem;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}

.race-toggle-btn + .race-toggle-btn { border-left: 1px solid var(--color-border); }
.race-toggle-active { background-color: var(--color-accent); color: white !important; }

.race-chart {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.race-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.8rem;
}

.race-rank {
  font-family: 'Space Mono', monospace;
  color: var(--color-text-muted);
  width: 28px;
  flex-shrink: 0;
  font-size: 0.7rem;
}

.race-name {
  width: 140px;
  flex-shrink: 0;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.race-track {
  flex: 1;
  height: 22px;
  background-color: var(--color-surface-2);
  border-radius: 6px;
  overflow: hidden;
}

.race-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent), #a78bfa);
  border-radius: 6px;
}

.race-value {
  font-family: 'Space Mono', monospace;
  color: var(--color-text);
  width: 60px;
  text-align: right;
  flex-shrink: 0;
  font-size: 0.75rem;
}

.race-empty {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
}
</style>