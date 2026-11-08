<template>
  <div class="detail-page">

    <div class="page-header">
      <RouterLink to="/projects" class="back-link">← My Projects</RouterLink>
      <div class="header-row">
        <h1 class="page-title">{{ form.projectName || 'Project' }}</h1>
        <div class="header-actions">
          <button v-if="dirty" @click="saveChanges" class="btn-primary" :disabled="saving">
            {{ saving ? 'Saving...' : 'Save Changes' }}
          </button>
          <span v-if="saveSuccess" class="save-success">✓ Saved</span>
        </div>
      </div>
    </div>

    <div v-if="loading" class="skeleton-block"></div>

    <div v-else-if="!project" class="empty-state">
      <span class="empty-icon">❌</span>
      <p>Project not found.</p>
      <RouterLink to="/projects" class="btn-primary">Go back</RouterLink>
    </div>

    <template v-else>

      <section class="card section">
        <h2 class="section-title">Project Details</h2>
        <p class="section-desc">Edit any field and click Save Changes.</p>

        <div class="fields-grid">

          <div class="field">
            <label class="field-label">Project Name</label>
            <input v-model="form.projectName" class="input" type="text" @input="dirty = true" />
          </div>

          <div class="field">
            <label class="field-label">Target Genre</label>
            <div class="custom-select" ref="genreRef">
              <button type="button" class="input custom-trigger" @click.stop="toggle('genre')">
                <span :style="{ color: form.targetGenre ? 'var(--color-text)' : 'var(--color-text-muted)' }">
                  {{ form.targetGenre || 'Select genre...' }}
                </span>
                <span>▾</span>
              </button>
              <div v-if="openDropdown === 'genre'" class="custom-dropdown">
                <div
                    v-for="g in genres" :key="g"
                    class="custom-option"
                    :class="{ selected: form.targetGenre === g }"
                    @click="pick('targetGenre', g, 'genre'); onGenreChange()"
                >{{ g }}</div>
              </div>
            </div>
          </div>

          <div class="field">
            <label class="field-label">Target Platform <span class="optional">(optional)</span></label>
            <div class="custom-select" ref="platformRef">
              <button type="button" class="input custom-trigger" @click.stop="toggle('platform')">
                <span :style="{ color: form.targetPlatform ? 'var(--color-text)' : 'var(--color-text-muted)' }">
                  {{ form.targetPlatform || 'No platform specified' }}
                </span>
                <span>▾</span>
              </button>
              <div v-if="openDropdown === 'platform'" class="custom-dropdown">
                <div class="custom-option" :class="{ selected: !form.targetPlatform }" @click="pick('targetPlatform', '', 'platform'); dirty = true">No platform specified</div>
                <div
                    v-for="p in platforms" :key="p.id"
                    class="custom-option"
                    :class="{ selected: form.targetPlatform === p.name }"
                    @click="pick('targetPlatform', p.name, 'platform'); dirty = true"
                >{{ p.name }}</div>
              </div>
            </div>
          </div>

          <div class="field">
            <label class="field-label">Base Region <span class="optional">(optional)</span></label>
            <div class="custom-select" ref="regionRef">
              <button type="button" class="input custom-trigger" @click.stop="toggle('region')">
                <span :style="{ color: form.expectedSalesRegion ? 'var(--color-text)' : 'var(--color-text-muted)' }">
                  {{ regionLabel || 'Select region...' }}
                </span>
                <span>▾</span>
              </button>
              <div v-if="openDropdown === 'region'" class="custom-dropdown">
                <div class="custom-option" :class="{ selected: !form.expectedSalesRegion }" @click="pick('expectedSalesRegion', '', 'region'); dirty = true">Select region...</div>
                <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'NA' }" @click="pick('expectedSalesRegion', 'NA', 'region'); dirty = true">NA — North America</div>
                <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'EU' }" @click="pick('expectedSalesRegion', 'EU', 'region'); dirty = true">EU — Europe</div>
                <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'JP' }" @click="pick('expectedSalesRegion', 'JP', 'region'); dirty = true">JP — Japan</div>
              </div>
            </div>
            <span class="field-hint">Required only for Revenue Predictor analysis.</span>
          </div>

          <div class="field">
            <label class="field-label">Expected Sales in Region (USD millions) <span class="optional">(optional)</span></label>
            <input v-model.number="form.expectedSalesAmount" class="input" type="number" min="0.01" step="0.01" @input="dirty = true" @keydown="blockInvalidKeys" />
            <span class="field-hint">Required only for Revenue Predictor analysis.</span>
          </div>

        </div>

        <div v-if="saveError" class="error-msg">{{ saveError }}</div>
      </section>

      <section class="card section">
        <div class="section-header">
          <div>
            <h2 class="section-title">Top Competitors</h2>
            <p class="section-desc">
              Best performing games with the same parameters
              (<strong class="accent">{{ form.targetGenre }}</strong><template v-if="form.targetPlatform">, <strong class="accent">{{ form.targetPlatform }}</strong></template>)
              ranked by weighted success score.
            </p>
          </div>
        </div>

        <div v-if="analytics && analytics.topCompetitors && analytics.topCompetitors.length" class="top-games-grid">
          <RouterLink
              v-for="game in analytics.topCompetitors"
              :key="game.id"
              :to="`/games/${game.id}`"
              class="top-game-card"
          >
            <div class="top-game-header">
              <span class="badge badge-purple">{{ game.genre }}</span>
              <span class="top-game-score">⭐ {{ game.criticScore }}</span>
            </div>
            <div class="top-game-title">{{ game.title }}</div>
            <div class="top-game-meta">
              <span>{{ game.platformName }}</span>
              <span>{{ game.yearOfRelease }}</span>
            </div>
          </RouterLink>
        </div>
        <div v-else-if="analytics && !analyticsLoading" class="no-competitors">
          No competitor data found - games in this category may not have reviews.
        </div>
      </section>

      <section class="card section">
        <div class="section-header">
          <div>
            <h2 class="section-title">Project Analytics</h2>
            <p class="section-desc">
              Showing data for <strong class="accent">{{ form.targetGenre || '—' }}</strong><template v-if="form.targetPlatform">, <strong class="accent">{{ form.targetPlatform }}</strong></template>.
            </p>
          </div>
          <div v-if="analyticsLoading" class="loading-badge">Loading...</div>
        </div>

        <div v-if="rateLimited" class="rate-limit-banner">
          ⚠️ Rate limit reached. Please wait a moment.
        </div>

        <div v-if="analyticsError" class="error-msg">{{ analyticsError }}</div>

        <div v-if="analytics" class="analytics-cards">

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🔮</span>
              <div>
                <div class="analytics-card-title">Revenue Predictor</div>
                <div class="analytics-card-desc">Based on your expected sales in {{ form.expectedSalesRegion || 'a region' }}, we estimate what you could make in the other two regions.</div>
              </div>
            </div>
            <div v-if="!form.expectedSalesRegion || !form.expectedSalesAmount" class="no-data-hint">
              Set Base Region and Expected Sales (USD millions) to see prediction.
            </div>
            <div v-else-if="analytics.predictedNa != null" class="analytic-result">
              <div class="revenue-breakdown">
                <div class="revenue-row">
                  <span class="revenue-region region-na">NA</span>
                  <span class="result-value">{{ analytics.predictedNa }}M USD</span>
                </div>
                <div class="revenue-row">
                  <span class="revenue-region region-eu">EU</span>
                  <span class="result-value">{{ analytics.predictedEu }}M USD</span>
                </div>
                <div class="revenue-row">
                  <span class="revenue-region region-jp">JP</span>
                  <span class="result-value">{{ analytics.predictedJp }}M USD</span>
                </div>
              </div>
            </div>
            <div v-else class="no-data-hint">No games found with the same parameters.</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🌍</span>
              <div>
                <div class="analytics-card-title">Global Adaptability</div>
                <div class="analytics-card-desc">Shows how balanced the sales are across NA, EU and JP. A high score means the game sells well everywhere, not just in one region.</div>
              </div>
            </div>
            <div v-if="analytics.globalAdaptability != null" class="analytic-result">
              <div class="progress-wrap">
                <div class="progress-track">
                  <div class="progress-fill" :class="adaptabilityClass" :style="{ width: analytics.globalAdaptability + '%' }"></div>
                </div>
                <span class="progress-value">{{ analytics.globalAdaptability.toFixed(1) }} / 100</span>
              </div>
              <span class="result-hint">{{ adaptabilityLabel }}</span>
            </div>
            <div v-else class="no-data-hint">No games found with the same parameters.</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">📍</span>
              <div>
                <div class="analytics-card-title">Region Priority</div>
                <div class="analytics-card-desc">Which region historically buys the most {{ form.targetGenre }} games — a good indicator of where to focus your launch.</div>
              </div>
            </div>
            <div v-if="analytics.regionPriority != null" class="analytic-result">
              <span class="region-badge" :class="regionClass">{{ analytics.regionPriority }}</span>
              <span class="result-hint">Focus market for {{ form.targetGenre }} games</span>
            </div>
            <div v-else class="no-data-hint">No games found with the same parameters.</div>
          </div>

          <div class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">⚠️</span>
              <div>
                <div class="analytics-card-title">Investment Risk</div>
                <div class="analytics-card-desc">How often well-reviewed games in this category still underperformed in sales. High percentage means critical success doesn't guarantee commercial success.</div>
              </div>
            </div>
            <div v-if="analytics.investmentRisk != null" class="analytic-result">
              <span class="result-value" :class="riskClass">{{ analytics.investmentRisk.toFixed(1) }}%</span>
              <span class="result-hint">{{ riskLabel }}</span>
            </div>
            <div v-else class="no-data-hint">No games found with the same parameters.</div>
          </div>
          <div v-if="analytics.platformRecommendation != null" class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🎮</span>
              <div>
                <div class="analytics-card-title">Platform Recommendation</div>
                <div class="analytics-card-desc">Best platform for {{ form.targetGenre }} games based on historical sales and critic scores.</div>
              </div>
            </div>
            <div class="analytic-result">
              <span class="result-value">{{ analytics.platformRecommendation }}</span>
              <span class="result-hint">Recommended platform for {{ form.targetGenre }} games</span>
            </div>
          </div>
          <div v-else-if="!form.targetPlatform" class="analytics-card">
            <div class="analytics-card-header">
              <span class="analytics-icon">🎮</span>
              <div>
                <div class="analytics-card-title">Platform Recommendation</div>
                <div class="analytics-card-desc">Best platform for {{ form.targetGenre }} games based on historical sales and critic scores.</div>
              </div>
            </div>
            <div class="no-data-hint">No games found with the same parameters.</div>
          </div>
        </div>
      </section>

    </template>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'

const route = useRoute()

const project  = ref(null)
const genres   = ref([])
const platforms = ref([])
const loading  = ref(true)

const dirty       = ref(false)
const saving      = ref(false)
const saveSuccess = ref(false)
const saveError   = ref('')

const analytics        = ref(null)
const analyticsLoading = ref(false)
const analyticsError   = ref('')
const rateLimited      = ref(false)

const openDropdown = ref(null)
const genreRef    = ref(null)
const platformRef = ref(null)
const regionRef   = ref(null)

const form = reactive({
  projectName:         '',
  targetGenre:         '',
  targetPlatform:      '',
  expectedSalesRegion: '',
  expectedSalesAmount: '',
})

const regionLabel = computed(() => {
  switch (form.expectedSalesRegion) {
    case 'NA': return 'NA — North America'
    case 'EU': return 'EU — Europe'
    case 'JP': return 'JP — Japan'
    default:   return ''
  }
})

function toggle(name) {
  openDropdown.value = openDropdown.value === name ? null : name
}

function pick(field, value) {
  form[field] = value
  openDropdown.value = null
}

function onClickOutside(e) {
  const refs = [genreRef.value, platformRef.value, regionRef.value]
  if (refs.every(r => !r || !r.contains(e.target))) {
    openDropdown.value = null
  }
}

function blockInvalidKeys(e) {
  if (['-', '+', 'e', 'E'].includes(e.key)) {
    e.preventDefault()
  }
}
onMounted(async () => {
  document.addEventListener('mousedown', onClickOutside)
  try {
    const [projectsRes, genresRes, platformsRes] = await Promise.all([
      api.get('/projects'),
      api.get('/games/genres'),
      api.get('/platforms/names'),
    ])
    genres.value   = genresRes.data ?? []
    platforms.value = platformsRes.data.data ?? []
    const all   = projectsRes.data.data ?? []
    const found = all.find(p => p.id == route.params.id)
    if (found) {
      project.value = found
      form.projectName         = found.projectName ?? ''
      form.targetGenre         = found.targetGenre ?? ''
      form.targetPlatform      = found.targetPlatform ?? ''
      form.expectedSalesRegion = found.expectedSalesRegion ?? ''
      form.expectedSalesAmount = found.expectedSalesAmount ?? ''
      await loadAnalytics()
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => document.removeEventListener('mousedown', onClickOutside))

async function loadAnalytics() {
  analyticsLoading.value = true
  analyticsError.value   = ''
  rateLimited.value      = false
  analytics.value        = null
  try {
    const res = await api.get(`/analytics/project/${route.params.id}`)
    analytics.value = res.data
  } catch (e) {
    if (e.response?.status === 429) {
      rateLimited.value    = true
      analyticsError.value = 'Rate limit reached.'
    } else if (e.response?.status === 404) {
      analyticsError.value = 'No data found for this project.'
    } else {
      analyticsError.value = 'Failed to load analytics.'
    }
  } finally {
    analyticsLoading.value = false
  }
}

function onGenreChange() {
  dirty.value = true
}

async function saveChanges() {
  saving.value    = true
  saveError.value = ''
  saveSuccess.value = false

  const payload = {
    projectName:         form.projectName,
    targetGenre:         form.targetGenre,
    targetPlatform:      form.targetPlatform || null,
    expectedSalesRegion: (form.expectedSalesAmount && form.expectedSalesRegion) ? form.expectedSalesRegion : null,
    expectedSalesAmount: form.expectedSalesAmount !== '' ? form.expectedSalesAmount : null,
  }

  try {
    await api.put(`/projects/${route.params.id}`, payload)
    dirty.value       = false
    saveSuccess.value = true
    setTimeout(() => { saveSuccess.value = false }, 2500)
    await loadAnalytics()
  } catch (e) {
    if (e.response?.status === 429) {
      saveError.value = 'Rate limit reached. Try again later.'
    } else {
      saveError.value = 'Failed to save. Please try again.'
    }
  } finally {
    saving.value = false
  }
}

const adaptabilityClass = computed(() => {
  const v = analytics.value?.globalAdaptability
  if (v == null) return ''
  if (v >= 70) return 'prog-good'
  if (v >= 40) return 'prog-ok'
  return 'prog-bad'
})

const adaptabilityLabel = computed(() => {
  const v = analytics.value?.globalAdaptability
  if (v == null) return ''
  if (v >= 70) return 'Sells evenly across all regions'
  if (v >= 40) return 'Moderate regional spread'
  return 'Highly concentrated in one region'
})

const regionClass = computed(() => {
  const r = analytics.value?.regionPriority
  if (r === 'NA') return 'region-na'
  if (r === 'EU') return 'region-eu'
  if (r === 'JP') return 'region-jp'
  return ''
})

const riskClass = computed(() => {
  const v = analytics.value?.investmentRisk
  if (v == null) return ''
  if (v >= 60) return 'score-bad'
  if (v >= 35) return 'score-ok'
  return 'score-good'
})

const riskLabel = computed(() => {
  const v = analytics.value?.investmentRisk
  if (v == null) return ''
  if (v >= 60) return 'High risk — many well-reviewed games underperformed'
  if (v >= 35) return 'Moderate risk'
  return 'Low risk — good reviews tend to correlate with sales'
})
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.back-link {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color 0.15s;
}

.back-link:hover { color: var(--color-accent); }

.page-header {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.page-title {
  font-size: 1.75rem;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.save-success {
  font-size: 0.875rem;
  color: #86efac;
  font-weight: 500;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.section-title {
  font-size: 1.1rem;
  margin: 0;
  color: var(--color-text);
}

.section-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0.25rem 0 0;
}

.accent { color: var(--color-accent); }

.loading-badge {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  padding: 0.25rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 999px;
}

.fields-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.78rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.optional {
  text-transform: none;
  font-weight: 400;
  font-size: 0.72rem;
  opacity: 0.7;
}

.field-hint {
  font-size: 0.72rem;
  color: var(--color-text-muted);
  opacity: 0.7;
}

.custom-select { position: relative; }

.custom-trigger {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  text-align: left;
}

.custom-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  z-index: 50;
  max-height: 180px;
  overflow-y: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.custom-option {
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  color: var(--color-text);
  cursor: pointer;
}

.custom-option:hover { background-color: var(--color-surface-2); }
.custom-option.selected { color: var(--color-accent); }

.top-games-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.top-game-card {
  background-color: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  text-decoration: none;
  transition: border-color 0.15s, transform 0.15s;
}

.top-game-card:hover {
  border-color: var(--color-accent);
  transform: translateY(-2px);
}

.top-game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-game-score {
  font-size: 0.8rem;
  color: #fcd34d;
  font-family: 'Space Mono', monospace;
}

.top-game-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.3;
}

.top-game-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.775rem;
  color: var(--color-text-muted);
}

.no-competitors {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  font-style: italic;
  padding: 1rem 0;
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

.revenue-breakdown {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.revenue-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.revenue-region {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 24px;
  border-radius: 6px;
  font-family: 'Space Mono', monospace;
  font-size: 0.7rem;
  font-weight: 700;
  flex-shrink: 0;
}

.no-data-hint {
  font-size: 0.775rem;
  color: var(--color-text-muted);
  padding-top: 0.5rem;
  border-top: 1px solid var(--color-border);
  font-style: italic;
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

.score-good { color: #86efac; }
.score-ok   { color: #fcd34d; }
.score-bad  { color: #fca5a5; }

.error-msg {
  font-size: 0.8rem;
  color: #fca5a5;
  padding: 0.5rem 0.75rem;
  background-color: rgba(239, 68, 68, 0.1);
  border-radius: 6px;
}

.rate-limit-banner {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background-color: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  color: #fcd34d;
  font-size: 0.875rem;
}

.skeleton-block {
  height: 300px;
  border-radius: 12px;
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

.empty-icon { font-size: 3rem; }

.btn-primary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: background-color 0.15s;
  text-decoration: none;
  display: inline-block;
}

.btn-primary:hover:not(:disabled) { background-color: var(--color-accent-hover); }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }
</style>