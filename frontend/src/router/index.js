import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'

import HomeView from '@/views/HomeView.vue'
import GamesView from '@/views/GamesView.vue'
import GameDetailView from '@/views/GameDetailView.vue'
import AnalyticsView from '@/views/AnalyticsView.vue'
import ProjectsView from '@/views/ProjectsView.vue'
import AdminView from '@/views/AdminView.vue'
import ProjectDetailView from '@/views/ProjectDetailView.vue'

const routes = [
  {
    path: '/',
    component: HomeView,
    meta: { title: 'Home' }
  },
  {
    path: '/games',
    component: GamesView,
    meta: { title: 'Games' }
  },
  {
    path: '/games/:id',
    component: GameDetailView,
    meta: { title: 'Game Detail' }
  },
  {
    path: '/analytics',
    component: AnalyticsView,
    meta: { title: 'Analytics', requiresAuth: true }
  },
  {
    path: '/projects',
    component: ProjectsView,
    meta: { title: 'My Projects', requiresAuth: true }
  },
  {
    path: '/projects/:id',
    component: ProjectDetailView,
    meta: { title: 'Project', requiresAuth: true }
  },
  {
    path: '/admin',
    component: AdminView,
    meta: { title: 'Admin', requiresAuth: true, requiresAdmin: true }
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  document.title = to.meta.title ? `${to.meta.title} | GameAnalyzer` : 'GameAnalyzer'

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { path: '/' }
  }

  if (to.meta.requiresAuth && !auth.isUser) {
    auth.login()
    return false
  }
})

export default router