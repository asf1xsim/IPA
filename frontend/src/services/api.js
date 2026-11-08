import axios from 'axios'
import keycloak from './keycloak'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
})

api.interceptors.request.use(async (config) => {
  if (keycloak.authenticated) {
    try {
      await keycloak.updateToken(30)
    } catch {
      keycloak.login()
    }
    console.log('token parsed:', keycloak.tokenParsed)
    config.headers.Authorization = `Bearer ${keycloak.token}`
  }
  return config
})

api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        keycloak.login()
      }
      return Promise.reject(error)
    }
)

export default api