<template>
  <div class="games-page">

    <div class="page-header">
      <h1 class="page-title">Games Catalog</h1>
      <span class="page-count" v-if="!loading">{{ filteredGames.length.toLocaleString() }} games</span>
    </div>

    <div class="filters-bar">
      <input
          v-model="searchQuery"
          class="input search-input"
          type="text"
          placeholder="Search by title..."
      />
      <select v-model="selectedGenre" class="input genre-select">
        <option value="">All genres</option>
        <option v-for="genre in genres" :key="genre" :value="genre">{{ genre }}</option>
      </select>
      <select v-model="selectedPlatform" class="input platform-select">
        <option value="">All platforms</option>
        <option v-for="p in platforms" :key="p" :value="p">{{ p }}</option>
      </select>
      <select v-model="sortBy" class="input sort-select">
        <option value="title">Sort: Title</option>
        <option value="year">Sort: Year</option>
        <option value="criticScore">Sort: Critic Score</option>
        <option value="globalSales">Sort: Global Sales</option>
      </select>
      <button v-if="searchQuery || selectedGenre || selectedPlatform" @click="resetFilters" class="btn-reset">
        ✕ Clear
      </button>
    </div>

    <div v-if="compareList.length > 0" class="compare-bar">
      <span class="compare-bar-text">{{ compareList.length }} game{{ compareList.length > 1 ? 's' : '' }} selected</span>
      <button class="btn-compare" :disabled="compareList.length < 2" @click="showCompare = true">
        Compare ({{ compareList.length }})
      </button>
      <button class="btn-compare-clear" @click="compareList = []">✕ Clear</button>
    </div>

    <div v-if="loading" class="skeleton-grid">
      <div v-for="i in 12" :key="i" class="skeleton-card"></div>
    </div>

    <div v-else-if="filteredGames.length === 0" class="empty-state">
      <span class="empty-icon">🔍</span>
      <p>No games found for your search.</p>
      <button @click="resetFilters" class="btn-primary">Clear filters</button>
    </div>

    <div v-else>
      <div class="table-outer" @mouseleave="hoverGame = null">
        <div class="games-table-wrap">
          <table class="games-table">
            <thead>
            <tr>
              <th class="col-check"></th>
              <th>Title</th>
              <th>Genre</th>
              <th>Platform</th>
              <th>Year</th>
              <th>Publisher</th>
              <th>Critic</th>
              <th>NA Sales</th>
              <th>EU Sales</th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="game in paginatedGames"
                :key="game.id"
                class="game-row"
                :class="{ 'row-selected': compareList.includes(game.id) }"
                @click="handleRowClick(game, $event)"
                @mouseenter="hoverGame = game"
            >
              <td class="col-check" @click.stop>
                <input
                    type="checkbox"
                    class="compare-check"
                    :checked="compareList.includes(game.id)"
                    @change="toggleCompare(game)"
                />
              </td>
              <td class="game-title-cell">{{ game.title }}</td>
              <td><span class="badge badge-purple">{{ game.genre }}</span></td>
              <td class="text-muted">{{ game.platformName }}</td>
              <td class="text-muted">{{ game.yearOfRelease ?? '—' }}</td>
              <td class="text-muted publisher-cell">{{ game.publisher ?? '—' }}</td>
              <td>
                <span v-if="game.criticScore" :class="scoreClass(game.criticScore)">
                  {{ game.criticScore }}
                </span>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <div v-if="game.naSales != null" class="sales-cell">
                  <span class="sales-num">{{ game.naSales }}M</span>
                  <div class="sales-track">
                    <div class="sales-fill na-fill" :style="{ width: pct(game.naSales, maxNaSales) + '%' }"></div>
                  </div>
                </div>
                <span v-else class="text-muted">—</span>
              </td>
              <td>
                <div v-if="game.euSales != null" class="sales-cell">
                  <span class="sales-num">{{ game.euSales }}M</span>
                  <div class="sales-track">
                    <div class="sales-fill eu-fill" :style="{ width: pct(game.euSales, maxEuSales) + '%' }"></div>
                  </div>
                </div>
                <span v-else class="text-muted">—</span>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <Transition name="hover-fade">
          <div v-if="hoverGame" class="hover-card">
            <div class="hover-title">{{ hoverGame.title }}</div>
            <div class="hover-rows">
              <div class="hover-row">
                <span class="hover-label">Global Sales</span>
                <span class="hover-val">{{ globalSales(hoverGame) }}M</span>
              </div>
              <div class="hover-row">
                <span class="hover-label">JP Sales</span>
                <span class="hover-val">{{ hoverGame.jpSales != null ? hoverGame.jpSales + 'M' : '—' }}</span>
              </div>
              <div class="hover-row">
                <span class="hover-label">User Score</span>
                <span class="hover-val">{{ hoverGame.userScore ?? '—' }}</span>
              </div>
              <div class="hover-row">
                <span class="hover-label">Developer</span>
                <span class="hover-val hover-val-sm">{{ hoverGame.developer ?? '—' }}</span>
              </div>
              <div class="hover-row">
                <span class="hover-label">Rating</span>
                <span class="hover-val">{{ hoverGame.rating ?? '—' }}</span>
              </div>
            </div>
          </div>
        </Transition>
      </div>

      <div class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">← Prev</button>
        <div class="page-numbers">
          <button
              v-for="p in visiblePages"
              :key="p"
              class="page-btn"
              :class="{ 'page-btn-active': p === currentPage }"
              @click="currentPage = p"
          >
            {{ p }}
          </button>
        </div>
        <button class="page-btn" :disabled="currentPage === totalPages" @click="currentPage++">Next →</button>
        <span class="page-info">Page {{ currentPage }} of {{ totalPages }}</span>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showCompare" class="modal-overlay" @click.self="showCompare = false">
        <div class="compare-modal">
          <div class="compare-modal-header">
            <h3 class="compare-modal-title">Compare Games</h3>
            <button class="modal-close-btn" @click="showCompare = false">✕</button>
          </div>
          <div class="compare-modal-body">
            <div class="compare-tags">
              <span v-for="g in selectedGamesData" :key="g.id" class="compare-tag">{{ g.title }}</span>
            </div>
            <div class="radar-wrap">
              <Radar :data="radarData" :options="radarOptions" />
            </div>
            <p class="radar-note">All axes normalized to 0–100. Sales relative to dataset max.</p>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Radar } from 'vue-chartjs'
import {
  Chart as ChartJS, RadialLinearScale, PointElement,
  LineElement, Filler, Tooltip, Legend
} from 'chart.js'
import api from '@/services/api.js'

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend)

const router = useRouter()

const allGames = ref([])
const genres = ref([])
const loading = ref(true)

const searchQuery = ref('')
const selectedGenre = ref('')
const selectedPlatform = ref('')
const sortBy = ref('title')
const currentPage = ref(1)
const perPage = 25

const hoverGame = ref(null)
const compareList = ref([])
const showCompare = ref(false)

onMounted(async () => {
  try {
    const [gamesRes, genresRes] = await Promise.all([
      api.get('/games'),
      api.get('/games/genres'),
    ])
    allGames.value = gamesRes.data.data ?? []
    genres.value = genresRes.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

const platforms = computed(() =>
    [...new Set(allGames.value.map(g => g.platformName).filter(Boolean))].sort()
)

const maxNaSales = computed(() => Math.max(...allGames.value.map(g => g.naSales ?? 0), 1))
const maxEuSales = computed(() => Math.max(...allGames.value.map(g => g.euSales ?? 0), 1))
const maxJpSales = computed(() => Math.max(...allGames.value.map(g => g.jpSales ?? 0), 1))

const filteredGames = computed(() => {
  let games = allGames.value

  if (selectedGenre.value)
    games = games.filter(g => g.genre === selectedGenre.value)

  if (selectedPlatform.value)
    games = games.filter(g => g.platformName === selectedPlatform.value)

  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase()
    games = games.filter(g => g.title?.toLowerCase().includes(q))
  }

  games = [...games].sort((a, b) => {
    if (sortBy.value === 'year') return (b.yearOfRelease ?? 0) - (a.yearOfRelease ?? 0)
    if (sortBy.value === 'criticScore') return (b.criticScore ?? 0) - (a.criticScore ?? 0)
    if (sortBy.value === 'globalSales') return globalSales(b) - globalSales(a)
    return (a.title ?? '').localeCompare(b.title ?? '')
  })

  return games
})

const totalPages = computed(() => Math.ceil(filteredGames.value.length / perPage))

const paginatedGames = computed(() => {
  const start = (currentPage.value - 1) * perPage
  return filteredGames.value.slice(start, start + perPage)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const cur = currentPage.value
  const pages = []
  let start = Math.max(1, cur - 2)
  let end = Math.min(total, start + 4)
  if (end - start < 4) start = Math.max(1, end - 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const selectedGamesData = computed(() =>
    compareList.value.map(id => allGames.value.find(g => g.id === id)).filter(Boolean)
)

const radarData = computed(() => {
  const colors = ['rgba(108,99,255,0.35)', 'rgba(34,197,94,0.35)', 'rgba(245,158,11,0.35)']
  const borders = ['#6c63ff', '#22c55e', '#f59e0b']
  return {
    labels: ['Critic Score', 'NA Sales', 'EU Sales', 'JP Sales', 'Recency'],
    datasets: selectedGamesData.value.map((g, i) => ({
      label: g.title,
      data: [
        g.criticScore ?? 0,
        pct(g.naSales ?? 0, maxNaSales.value),
        pct(g.euSales ?? 0, maxEuSales.value),
        pct(g.jpSales ?? 0, maxJpSales.value),
        g.yearOfRelease ? pct(g.yearOfRelease - 1980, 36) : 0,
      ],
      backgroundColor: colors[i],
      borderColor: borders[i],
      borderWidth: 2,
      pointBackgroundColor: borders[i],
    })),
  }
})

const radarOptions = {
  responsive: true,
  maintainAspectRatio: true,
  scales: {
    r: {
      min: 0,
      max: 100,
      ticks: { display: false },
      grid: { color: 'rgba(255,255,255,0.08)' },
      pointLabels: { color: '#a1a1aa', font: { size: 12 } },
      angleLines: { color: 'rgba(255,255,255,0.08)' },
    },
  },
  plugins: {
    legend: { labels: { color: '#e4e4e7', font: { size: 11 }, boxWidth: 12 } },
  },
}

watch([searchQuery, selectedGenre, selectedPlatform, sortBy], () => {
  currentPage.value = 1
})

function resetFilters() {
  searchQuery.value = ''
  selectedGenre.value = ''
  selectedPlatform.value = ''
  currentPage.value = 1
}

function scoreClass(score) {
  if (score >= 80) return 'score score-good'
  if (score >= 60) return 'score score-ok'
  return 'score score-bad'
}

function pct(value, max) {
  if (!max) return 0
  return Math.min((value / max) * 100, 100)
}

function globalSales(g) {
  return +((g.naSales ?? 0) + (g.euSales ?? 0) + (g.jpSales ?? 0)).toFixed(2)
}

function toggleCompare(game) {
  const idx = compareList.value.indexOf(game.id)
  if (idx !== -1) {
    compareList.value.splice(idx, 1)
  } else if (compareList.value.length < 3) {
    compareList.value.push(game.id)
  }
}

function handleRowClick(game, event) {
  if (event.target.type === 'checkbox') return
  router.push(`/games/${game.id}`)
}
</script>

<style scoped>
.games-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  align-items: baseline;
  gap: 1rem;
}

.page-title {
  font-size: 1.75rem;
  margin: 0;
}

.page-count {
  font-family: 'Space Mono', monospace;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.filters-bar {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  align-items: center;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.genre-select,
.platform-select,
.sort-select {
  width: auto;
  min-width: 140px;
}

.btn-reset {
  padding: 0.5rem 0.875rem;
  border-radius: 8px;
  font-size: 0.875rem;
  color: var(--color-text-muted);
  background: transparent;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}

.btn-reset:hover {
  color: var(--color-danger);
  border-color: var(--color-danger);
}

.compare-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 1rem;
  background-color: rgba(108, 99, 255, 0.1);
  border: 1px solid rgba(108, 99, 255, 0.3);
  border-radius: 10px;
}

.compare-bar-text {
  font-size: 0.875rem;
  color: #a5b4fc;
  flex: 1;
}

.btn-compare {
  padding: 0.4rem 1rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.8rem;
  font-weight: 500;
  transition: background-color 0.15s;
}

.btn-compare:hover:not(:disabled) {
  background-color: var(--color-accent-hover);
}

.btn-compare:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-compare-clear {
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  background: transparent;
  color: #a5b4fc;
  border: 1px solid rgba(108, 99, 255, 0.3);
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.15s;
}

.btn-compare-clear:hover {
  border-color: #a5b4fc;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
}

.skeleton-card {
  height: 80px;
  border-radius: 8px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  text-align: center;
  padding: 4rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: var(--color-text-muted);
}

.empty-icon {
  font-size: 3rem;
}

.table-outer {
  position: relative;
}

.games-table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: 12px;
}

.games-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.games-table thead tr {
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.games-table th {
  padding: 0.875rem 1rem;
  text-align: left;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  white-space: nowrap;
}

.col-check {
  width: 36px;
  padding: 0 0 0 1rem !important;
}

.compare-check {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: var(--color-accent);
}

.game-row {
  border-bottom: 1px solid var(--color-border);
  cursor: pointer;
  transition: background-color 0.1s;
}

.game-row:last-child {
  border-bottom: none;
}

.game-row:hover {
  background-color: var(--color-surface);
}

.row-selected {
  background-color: rgba(108, 99, 255, 0.06) !important;
}

.games-table td {
  padding: 0.75rem 1rem;
  vertical-align: middle;
}

.game-title-cell {
  font-weight: 500;
  color: var(--color-text);
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.publisher-cell {
  max-width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-muted {
  color: var(--color-text-muted);
}

.score {
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
}

.score-good { background-color: rgba(34, 197, 94, 0.15); color: #86efac; }
.score-ok   { background-color: rgba(245, 158, 11, 0.15); color: #fcd34d; }
.score-bad  { background-color: rgba(239, 68, 68, 0.15);  color: #fca5a5; }

.sales-cell {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 70px;
}

.sales-num {
  font-family: 'Space Mono', monospace;
  font-size: 0.75rem;
  color: var(--color-text);
}

.sales-track {
  width: 100%;
  height: 4px;
  background-color: var(--color-border);
  border-radius: 999px;
  overflow: hidden;
}

.sales-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.3s ease;
}

.na-fill { background-color: #6c63ff; }
.eu-fill { background-color: #22c55e; }

.hover-card {
  position: fixed;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  width: 200px;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1rem;
  z-index: 50;
  pointer-events: none;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.hover-title {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 0.75rem;
  line-height: 1.3;
  word-break: break-word;
}

.hover-rows {
  display: flex;
  flex-direction: column;
  gap: 0.45rem;
}

.hover-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.75rem;
}

.hover-label {
  color: var(--color-text-muted);
}

.hover-val {
  font-family: 'Space Mono', monospace;
  color: var(--color-text);
  font-size: 0.75rem;
}

.hover-val-sm {
  font-family: inherit;
  font-size: 0.7rem;
  max-width: 110px;
  text-align: right;
  word-break: break-word;
}

.hover-fade-enter-active,
.hover-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.hover-fade-enter-from,
.hover-fade-leave-to {
  opacity: 0;
  transform: translateY(calc(-50% + 6px));
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}

.page-numbers {
  display: flex;
  gap: 0.25rem;
}

.page-btn {
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s;
}

.page-btn:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.page-btn-active {
  background-color: var(--color-accent);
  border-color: var(--color-accent);
  color: white !important;
}

.page-info {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  margin-left: 0.5rem;
}

.btn-primary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 1rem;
}

.compare-modal {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  overflow: hidden;
}

.compare-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.compare-modal-title {
  font-size: 1rem;
  margin: 0;
}

.modal-close-btn {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 1rem;
  padding: 0.25rem;
  transition: color 0.15s;
}

.modal-close-btn:hover {
  color: var(--color-text);
}

.compare-modal-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.compare-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.compare-tag {
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background-color: rgba(108, 99, 255, 0.15);
  border: 1px solid rgba(108, 99, 255, 0.3);
  color: #a5b4fc;
  font-size: 0.775rem;
}

.radar-wrap {
  max-width: 380px;
  margin: 0 auto;
  width: 100%;
}

.radar-note {
  font-size: 0.725rem;
  color: var(--color-text-muted);
  text-align: center;
  margin: 0;
}
</style>