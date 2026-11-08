<template>
  <nav class="navbar">
    <div class="navbar-inner">

      <RouterLink to="/" class="navbar-logo">
        <span class="logo-icon">🎮</span>
        <span class="logo-text">GameAnalyzer</span>
      </RouterLink>

      <div class="navbar-links">
        <RouterLink to="/games" class="nav-link">Games</RouterLink>
        <RouterLink v-if="auth.isUser" to="/analytics" class="nav-link">Analytics</RouterLink>
        <RouterLink v-if="auth.isUser" to="/projects" class="nav-link">My Projects</RouterLink>
        <RouterLink v-if="auth.isAdmin" to="/admin" class="nav-link nav-link-admin">Admin</RouterLink>
      </div>

      <div class="navbar-auth">
        <template v-if="auth.authenticated">
          <div class="user-info">
            <span class="user-avatar">{{ auth.username.charAt(0).toUpperCase() }}</span>
            <span class="user-name">{{ auth.username }}</span>
            <span v-if="auth.isAdmin" class="badge badge-purple">admin</span>
          </div>
          <button @click="auth.logout()" class="btn-secondary btn-sm">Logout</button>
        </template>
        <template v-else>
          <button @click="auth.register()" class="btn-secondary btn-sm">Register</button>
          <button @click="auth.login()" class="btn-primary btn-sm">Login</button>
        </template>
      </div>

    </div>
  </nav>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
  backdrop-filter: blur(8px);
}

.navbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 1rem;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 2rem;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  flex-shrink: 0;
}

.logo-icon {
  font-size: 1.25rem;
}

.logo-text {
  font-family: 'Space Mono', monospace;
  font-size: 1rem;
  font-weight: 700;
  color: var(--color-accent);
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex: 1;
}

.nav-link {
  padding: 0.4rem 0.75rem;
  border-radius: 8px;
  font-size: 0.875rem;
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color 0.15s, background-color 0.15s;
}

.nav-link:hover {
  color: var(--color-text);
  background-color: var(--color-surface-2);
}

.nav-link.router-link-active {
  color: var(--color-accent);
  background-color: rgba(108, 99, 255, 0.1);
}

.nav-link-admin {
  color: #fcd34d;
}

.nav-link-admin:hover {
  color: #fbbf24;
  background-color: rgba(245, 158, 11, 0.1);
}

.nav-link-admin.router-link-active {
  color: #fbbf24;
  background-color: rgba(245, 158, 11, 0.1);
}

.navbar-auth {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background-color: var(--color-accent);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  font-family: 'Space Mono', monospace;
}

.user-name {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-sm {
  padding: 0.35rem 0.875rem;
  font-size: 0.8rem;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background-color 0.15s, border-color 0.15s;
}

.btn-primary {
  background-color: var(--color-accent);
  color: white;
}

.btn-primary:hover {
  background-color: var(--color-accent-hover);
}

.btn-secondary {
  background-color: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border) !important;
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted) !important;
}
</style>