<template>
  <div class="detail-page">

    <div v-if="loading" class="loading-state">
      <div class="skeleton-hero"></div>
      <div class="skeleton-grid">
        <div class="skeleton-block" v-for="i in 4" :key="i"></div>
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <span class="error-icon">⚠️</span>
      <p>Game not found.</p>
      <RouterLink to="/games" class="btn-primary">Back to catalog</RouterLink>
    </div>

    <template v-else>

      <div class="back-link-wrap">
        <button @click="router.back()" class="back-link">← Back</button>
      </div>

      <div class="hero-card">
        <div class="hero-left">
          <div class="hero-badges">
            <span class="badge badge-purple">{{ game.genre }}</span>
            <span class="badge badge-yellow" v-if="game.yearOfRelease">{{ game.yearOfRelease }}</span>
          </div>
          <h1 class="game-title">{{ game.title }}</h1>
          <div class="game-meta">
            <span class="meta-item">
              <span class="meta-label">Platform</span>
              <span class="meta-value">{{ game.platformName }}</span>
            </span>
            <span class="meta-divider">·</span>
            <span class="meta-item">
              <span class="meta-label">Publisher</span>
              <span class="meta-value">{{ game.publisher ?? 'Unknown' }}</span>
            </span>
          </div>
        </div>

        <div class="scores-wrap" v-if="game.criticScore != null || game.userScore != null">
          <div class="score-box" v-if="game.criticScore != null">
            <span class="score-number" :class="scoreClass(game.criticScore)">{{ game.criticScore }}</span>
            <span class="score-label">Critic Score</span>
          </div>
          <div class="score-box" v-if="game.userScore != null">
            <span class="score-number score-user">{{ game.userScore }}</span>
            <span class="score-label">User Score</span>
          </div>
        </div>
      </div>

      <div class="content-grid">

        <div class="card sales-card">
          <h2 class="card-title">Sales by Region</h2>
          <div v-if="hasSales" class="sales-bars">
            <div v-for="region in salesRegions" :key="region.label" class="sales-row">
              <span class="sales-label">{{ region.label }}</span>
              <div class="bar-track">
                <div
                    class="bar-fill"
                    :style="{ width: region.percent + '%', backgroundColor: region.color }"
                ></div>
              </div>
              <span class="sales-value">{{ region.value }}M USD</span>
            </div>
            <div class="sales-total">
              <span>Total (NA + EU + JP)</span>
              <span class="total-value">{{ totalSales }}M USD</span>
            </div>
          </div>
          <p v-else class="no-data">No sales data available.</p>
        </div>

        <div class="card info-card">
          <h2 class="card-title">Game Info</h2>
          <div class="info-rows">
            <div class="info-row">
              <span class="info-label">Title</span>
              <span class="info-value">{{ game.title }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Genre</span>
              <span class="info-value">{{ game.genre }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Platform</span>
              <span class="info-value">{{ game.platformName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Year</span>
              <span class="info-value">{{ game.yearOfRelease ?? '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Publisher</span>
              <span class="info-value">{{ game.publisher ?? '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Critic Score</span>
              <span class="info-value">{{ game.criticScore ?? '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">User Score</span>
              <span class="info-value">{{ game.userScore ?? '—' }}</span>
            </div>
          </div>
        </div>

      </div>

      <div class="analytics-cta card">
        <div class="cta-text">
          <h3 class="cta-title">Want deeper insights?</h3>
          <p class="cta-desc">Run a success score analysis on this game or explore genre-level analytics.</p>
        </div>
        <div class="cta-actions">
          <template v-if="auth.isUser">
            <RouterLink
                :to="{ path: '/analytics', query: { gameId: game.id, genre: game.genre } }"
                class="btn-primary"
            >
              Analyze this game
            </RouterLink>
          </template>
          <template v-else>
            <button @click="auth.login()" class="btn-primary">Login to Analyze</button>
          </template>
          <RouterLink to="/games" class="btn-secondary">Back to catalog</RouterLink>
        </div>
      </div>

    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const game = ref(null)
const loading = ref(true)
const error = ref(false)

onMounted(async () => {
  try {
    const res = await api.get(`/games/${route.params.id}`)
    game.value = res.data.data
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
})

const hasSales = computed(() =>
    game.value && (game.value.naSales != null || game.value.euSales != null || game.value.jpSales != null)
)

const maxSales = computed(() => {
  if (!game.value) return 1
  return Math.max(game.value.naSales ?? 0, game.value.euSales ?? 0, game.value.jpSales ?? 0) || 1
})

const salesRegions = computed(() => {
  if (!game.value) return []
  return [
    { label: 'NA', value: game.value.naSales ?? 0, color: '#6c63ff', percent: ((game.value.naSales ?? 0) / maxSales.value) * 100 },
    { label: 'EU', value: game.value.euSales ?? 0, color: '#22c55e', percent: ((game.value.euSales ?? 0) / maxSales.value) * 100 },
    { label: 'JP', value: game.value.jpSales ?? 0, color: '#f59e0b', percent: ((game.value.jpSales ?? 0) / maxSales.value) * 100 },
  ]
})

const totalSales = computed(() => {
  if (!game.value) return 0
  return (+(game.value.naSales ?? 0) + +(game.value.euSales ?? 0) + +(game.value.jpSales ?? 0)).toFixed(2)
})

function scoreClass(score) {
  if (score >= 80) return 'score-good'
  if (score >= 60) return 'score-ok'
  return 'score-bad'
}
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  max-width: 900px;
  margin: 0 auto;
}

.back-link-wrap {
  display: flex;
}

.back-link {
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 0.875rem;
  cursor: pointer;
  padding: 0;
  transition: color 0.15s;
}

.back-link:hover {
  color: var(--color-text);
}

.hero-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 2rem;
}

.hero-left {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  flex: 1;
}

.hero-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.game-title {
  font-size: clamp(1.5rem, 4vw, 2rem);
  margin: 0;
  line-height: 1.2;
  color: var(--color-text);
}

.game-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  gap: 0.4rem;
  font-size: 0.875rem;
}

.meta-label {
  color: var(--color-text-muted);
}

.meta-value {
  color: var(--color-text);
  font-weight: 500;
}

.meta-divider {
  color: var(--color-border);
}

.scores-wrap {
  display: flex;
  gap: 1rem;
  flex-shrink: 0;
}

.score-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.35rem;
  background-color: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  min-width: 80px;
}

.score-number {
  font-family: 'Space Mono', monospace;
  font-size: 1.75rem;
  font-weight: 700;
}

.score-good { color: #86efac; }
.score-ok   { color: #fcd34d; }
.score-bad  { color: #fca5a5; }
.score-user { color: #a5b4fc; }

.score-label {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  text-align: center;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.5rem;
}

.card-title {
  font-size: 1rem;
  margin: 0 0 1.25rem;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 0.75rem;
}

.sales-bars {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.sales-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.sales-label {
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  font-weight: 700;
  color: var(--color-text-muted);
  width: 28px;
  flex-shrink: 0;
}

.bar-track {
  flex: 1;
  height: 10px;
  background-color: var(--color-surface-2);
  border-radius: 999px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 999px;
  transition: width 0.6s ease;
}

.sales-value {
  font-family: 'Space Mono', monospace;
  font-size: 0.8rem;
  color: var(--color-text);
  width: 48px;
  text-align: right;
  flex-shrink: 0;
}

.sales-total {
  display: flex;
  justify-content: space-between;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.total-value {
  font-family: 'Space Mono', monospace;
  color: var(--color-text);
  font-weight: 700;
}

.no-data {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  margin: 0;
}

.info-rows {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--color-border);
  font-size: 0.875rem;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  color: var(--color-text-muted);
}

.info-value {
  color: var(--color-text);
  font-weight: 500;
  text-align: right;
  max-width: 55%;
  word-break: break-word;
}

.analytics-cta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.cta-title {
  font-size: 1rem;
  margin: 0 0 0.35rem;
  color: var(--color-text);
}

.cta-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0;
}

.cta-actions {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
  flex-wrap: wrap;
}

.btn-primary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: var(--color-accent);
  color: white;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: background-color 0.15s;
}

.btn-primary:hover {
  background-color: var(--color-accent-hover);
}

.btn-secondary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: all 0.15s;
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}

.loading-state {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.skeleton-hero {
  height: 160px;
  border-radius: 16px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}

.skeleton-block {
  height: 220px;
  border-radius: 12px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.error-state {
  text-align: center;
  padding: 4rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: var(--color-text-muted);
}

.error-icon {
  font-size: 3rem;
}
</style>