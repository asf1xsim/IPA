import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import keycloak from '@/services/keycloak.js'

export const useAuthStore = defineStore('auth', () => {
  const authenticated = ref(false)
  const username = ref('')
  const roles = ref([])

  function extractRoles(tokenParsed) {
    if (!tokenParsed) return []
    const clientRoles = tokenParsed.resource_access?.['backend-client']?.roles ?? []
    const realmRoles = tokenParsed.realm_access?.roles ?? []
    return [...new Set([...clientRoles, ...realmRoles])]
  }

  function setFromKeycloak() {
    authenticated.value = keycloak.authenticated ?? false
    username.value = keycloak.tokenParsed?.preferred_username ?? ''
    roles.value = extractRoles(keycloak.tokenParsed)
  }

  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const isUser = computed(() => authenticated.value || isAdmin.value)

  function login() {
    keycloak.login()
  }

  function logout() {
    keycloak.logout({ redirectUri: window.location.origin })
  }

  function register() {
    keycloak.register()
  }


  return { authenticated, username, roles, isAdmin, isUser, setFromKeycloak, login, logout, register }
})