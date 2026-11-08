import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import keycloak from './services/keycloak'
import { useAuthStore } from './stores/auth'
import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

keycloak.onAuthSuccess = () => {
  const auth = useAuthStore()
  auth.setFromKeycloak()
}

keycloak.onTokenExpired = () => {
  keycloak.updateToken(30).catch(() => keycloak.login())
}

keycloak.init({ onLoad: 'check-sso', silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html' })
.then(() => {
  const auth = useAuthStore()
  auth.setFromKeycloak()
  app.mount('#app')
})
.catch(() => {
  app.mount('#app')
})