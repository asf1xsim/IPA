<template>
  <div class="home">

    <section class="hero">
      <h1 class="hero-title">
        Indie Publisher<br>
        <span class="hero-accent">Assistant</span>
      </h1>
      <p class="hero-subtitle">
        Explore sales data from 16,000+ games across all major platforms.
        Predict revenue, assess investment risk and find your target market.
      </p>
      <div class="hero-actions">
        <RouterLink to="/games" class="btn-primary btn-lg">Browse Games</RouterLink>
        <RouterLink v-if="auth.isUser" to="/analytics" class="btn-secondary btn-lg">Open Analytics</RouterLink>
        <button v-else @click="auth.login()" class="btn-secondary btn-lg">Login to Analyze</button>
      </div>
    </section>

    <section class="stats-section">
      <div v-if="loading" class="stats-grid">
        <div v-for="i in 3" :key="i" class="stat-card stat-card-skeleton"></div>
      </div>
      <div v-else class="stats-grid">
        <div class="stat-card">
          <span class="stat-icon">🕹️</span>
          <span class="stat-number">{{ stats.games.toLocaleString() }}</span>
          <span class="stat-label">Games in database</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">🗂️</span>
          <span class="stat-number">{{ stats.genres }}</span>
          <span class="stat-label">Unique genres</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">📺</span>
          <span class="stat-number">{{ stats.platforms }}</span>
          <span class="stat-label">Platforms covered</span>
        </div>
      </div>
    </section>

    <section class="features-section">
      <h2 class="section-title">What you can do</h2>
      <div class="features-grid">

        <div class="feature-card">
          <div class="feature-icon">📊</div>
          <h3 class="feature-title">Browse Game Catalog</h3>
          <p class="feature-desc">
            Filter and explore the full dataset of games. View sales by region, critic scores, and publisher data.
          </p>
          <RouterLink to="/games" class="feature-link">Explore games →</RouterLink>
        </div>

        <div class="feature-card" :class="{ 'feature-card-locked': !auth.isUser }">
          <div class="feature-icon">🔮</div>
          <h3 class="feature-title">Analytics Engine</h3>
          <p class="feature-desc">
            Run revenue predictions, global adaptability scores, region priority analysis and investment risk assessment.
          </p>
          <RouterLink v-if="auth.isUser" to="/analytics" class="feature-link">Open analytics →</RouterLink>
          <button v-else @click="auth.login()" class="feature-link-locked">🔒 Login required</button>
        </div>

        <div class="feature-card" :class="{ 'feature-card-locked': !auth.isUser }">
          <div class="feature-icon">📁</div>
          <h3 class="feature-title">Project Workspace</h3>
          <p class="feature-desc">
            Save your research as projects. Define a target genre and platform, then analyze your specific market segment.
          </p>
          <RouterLink v-if="auth.isUser" to="/projects" class="feature-link">My projects →</RouterLink>
          <button v-else @click="auth.login()" class="feature-link-locked">🔒 Login required</button>
        </div>

      </div>
    </section>

    <section v-if="topGames.length" class="top-games-section">
      <h2 class="section-title">Top Rated Games</h2>
      <div class="top-games-grid">
        <RouterLink
            v-for="game in topGames"
            :key="game.id"
            :to="`/games/${game.id}`"
            class="top-game-card"
        >
          <div class="top-game-header">
            <span class="top-game-genre badge badge-purple">{{ game.genre }}</span>
            <span class="top-game-score">⭐ {{ game.criticScore }}</span>
          </div>
          <div class="top-game-title">{{ game.title }}</div>
          <div class="top-game-meta">
            <span>{{ game.platformName }}</span>
            <span>{{ game.yearOfRelease }}</span>
          </div>
        </RouterLink>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/services/api'

const auth = useAuthStore()
const loading = ref(true)

const stats = ref({ games: 0, genres: 0, platforms: 0 })
const topGames = ref([])

onMounted(async () => {
  try {
    const [statsRes, topRes] = await Promise.all([
      api.get('/games/stats'),
      api.get('/games/top'),
    ])
    stats.value.games     = statsRes.data.games ?? 0
    stats.value.genres    = statsRes.data.genres ?? 0
    stats.value.platforms = statsRes.data.platforms ?? 0
    topGames.value        = topRes.data.data ?? []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 4rem;
  padding-bottom: 4rem;
}

.hero {
  text-align: center;
  padding: 4rem 1rem 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
}

.hero-title {
  font-size: clamp(2.5rem, 6vw, 4rem);
  line-height: 1.1;
  margin: 0;
  color: var(--color-text);
}

.hero-accent {
  color: var(--color-accent);
}

.hero-subtitle {
  font-size: 1.1rem;
  color: var(--color-text-muted);
  max-width: 520px;
  line-height: 1.7;
  margin: 0;
}

.hero-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  justify-content: center;
}

.btn-lg {
  padding: 0.7rem 1.5rem;
  font-size: 0.95rem;
  border-radius: 10px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: none;
  border: none;
  transition: all 0.15s;
}

.btn-primary {
  background-color: var(--color-accent);
  color: white;
  display: inline-flex;
  align-items: center;
}

.btn-primary:hover { background-color: var(--color-accent-hover); }

.btn-secondary {
  background-color: transparent;
  color: var(--color-text);
  border: 1px solid var(--color-border) !important;
  display: inline-flex;
  align-items: center;
}

.btn-secondary:hover {
  border-color: var(--color-accent) !important;
  color: var(--color-accent);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.stat-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.75rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  text-align: center;
}

.stat-card-skeleton {
  min-height: 130px;
  background: linear-gradient(90deg, var(--color-surface) 25%, var(--color-surface-2) 50%, var(--color-surface) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.stat-icon { font-size: 1.75rem; }

.stat-number {
  font-family: 'Space Mono', monospace;
  font-size: 2rem;
  font-weight: 700;
  color: var(--color-accent);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.section-title {
  font-size: 1.5rem;
  margin: 0 0 1.5rem;
  color: var(--color-text);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.feature-card {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 1.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: border-color 0.2s;
}

.feature-card:hover { border-color: var(--color-accent); }
.feature-card-locked { opacity: 0.65; }
.feature-card-locked:hover { border-color: var(--color-border); }

.feature-icon { font-size: 1.75rem; }

.feature-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
}

.feature-desc {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  line-height: 1.6;
  margin: 0;
  flex: 1;
}

.feature-link {
  font-size: 0.875rem;
  color: var(--color-accent);
  text-decoration: none;
  font-weight: 500;
  margin-top: auto;
}

.feature-link:hover { text-decoration: underline; }

.feature-link-locked {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  text-align: left;
}

.top-games-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.top-game-card {
  background-color: var(--color-surface);
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
</style>