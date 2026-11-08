<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h2 class="modal-title">{{ project ? 'Edit Project' : 'New Project' }}</h2>
        <button class="modal-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="field">
          <label class="field-label">Project Name</label>
          <input v-model="form.projectName" class="input" type="text" placeholder="My Action Game Project" />
          <span v-if="formErrors.projectName" class="field-error">{{ formErrors.projectName }}</span>
        </div>

        <div class="field">
          <label class="field-label">Target Genre</label>
          <div class="custom-select" ref="genreSelectRef">
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
                  @click="pick('targetGenre', g, 'genre')"
              >{{ g }}</div>
            </div>
          </div>
          <span v-if="formErrors.targetGenre" class="field-error">{{ formErrors.targetGenre }}</span>
        </div>

        <div class="field">
          <label class="field-label">Target Platform <span class="optional">(optional)</span></label>
          <div class="custom-select" ref="platformSelectRef">
            <button type="button" class="input custom-trigger" @click.stop="toggle('platform')">
              <span :style="{ color: form.targetPlatform ? 'var(--color-text)' : 'var(--color-text-muted)' }">
                {{ form.targetPlatform || 'No platform specified' }}
              </span>
              <span>▾</span>
            </button>
            <div v-if="openDropdown === 'platform'" class="custom-dropdown">
              <div class="custom-option" @click="pick('targetPlatform', '', 'platform')">No platform specified</div>
              <div
                  v-for="p in platforms" :key="p.id"
                  class="custom-option"
                  :class="{ selected: form.targetPlatform === p.name }"
                  @click="pick('targetPlatform', p.name, 'platform')"
              >{{ p.name }}</div>
            </div>
          </div>
        </div>

        <div class="field">
          <label class="field-label">Base Region <span class="optional">(optional)</span></label>
          <div class="custom-select" ref="regionSelectRef">
            <button type="button" class="input custom-trigger" @click.stop="toggle('region')">
              <span :style="{ color: form.expectedSalesRegion ? 'var(--color-text)' : 'var(--color-text-muted)' }">
                {{ regionLabel || 'Select region...' }}
              </span>
              <span>▾</span>
            </button>
            <div v-if="openDropdown === 'region'" class="custom-dropdown">
              <div class="custom-option" @click="pick('expectedSalesRegion', '', 'region')">Select region...</div>
              <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'NA' }" @click="pick('expectedSalesRegion', 'NA', 'region')">NA — North America</div>
              <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'EU' }" @click="pick('expectedSalesRegion', 'EU', 'region')">EU — Europe</div>
              <div class="custom-option" :class="{ selected: form.expectedSalesRegion === 'JP' }" @click="pick('expectedSalesRegion', 'JP', 'region')">JP — Japan</div>
            </div>
          </div>
          <span class="field-hint">Required only for Revenue Predictor analysis.</span>
        </div>

        <div class="field">
          <label class="field-label">Expected Sales in Region (USD millions) <span class="optional">(optional)</span></label>
          <input v-model.number="form.expectedSalesAmount" class="input" type="number" min="0.01" step="0.01" placeholder="e.g. 1.5" @keydown="blockInvalidKeys"/>
          <span class="field-hint">Required only for Revenue Predictor analysis.</span>
          <span v-if="formErrors.expectedSalesAmount" class="field-error">{{ formErrors.expectedSalesAmount }}</span>
        </div>

        <div v-if="apiError" class="api-error">{{ apiError }}</div>
      </div>

      <div class="modal-footer">
        <button @click="$emit('close')" class="btn-secondary">Cancel</button>
        <button @click="submit" class="btn-primary" :disabled="loading">
          {{ loading ? 'Saving...' : (project ? 'Save Changes' : 'Create Project') }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import api from '@/services/api'

const props = defineProps({
  project:   { type: Object, default: null },
  genres:    { type: Array, default: () => [] },
  platforms: { type: Array, default: () => [] },
})

const emit = defineEmits(['close', 'saved'])

const loading      = ref(false)
const apiError     = ref('')
const openDropdown = ref(null)

const genreSelectRef    = ref(null)
const platformSelectRef = ref(null)
const regionSelectRef   = ref(null)

const form = reactive({
  projectName:         props.project?.projectName ?? '',
  targetGenre:         props.project?.targetGenre ?? '',
  targetPlatform:      props.project?.targetPlatform ?? '',
  expectedSalesRegion: props.project?.expectedSalesRegion ?? '',
  expectedSalesAmount: props.project?.expectedSalesAmount ?? '',
})

const formErrors = reactive({
  projectName: '',
  targetGenre: '',
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

function pick(field, value, dropdown) {
  form[field] = value
  openDropdown.value = null
}

function onClickOutside(e) {
  const refs = [genreSelectRef.value, platformSelectRef.value, regionSelectRef.value]
  if (refs.every(r => r && !r.contains(e.target))) {
    openDropdown.value = null
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onUnmounted(() => document.removeEventListener('mousedown', onClickOutside))

function validate() {
  let valid = true
  formErrors.projectName = ''
  formErrors.targetGenre = ''
  formErrors.expectedSalesAmount = ''

  if (!form.projectName.trim()) {
    formErrors.projectName = 'Project name is required'
    valid = false
  }
  if (!form.targetGenre) {
    formErrors.targetGenre = 'Please select a genre'
    valid = false
  }
  if (form.expectedSalesAmount !== '' && (isNaN(form.expectedSalesAmount) || Number(form.expectedSalesAmount) <= 0)) {
    formErrors.expectedSalesAmount = 'Enter a valid amount greater than 0'
    valid = false
  }
  if (form.expectedSalesAmount !== '' && Number(form.expectedSalesAmount) > 0 && !form.expectedSalesRegion) {
    formErrors.expectedSalesAmount = 'Please select a base region when entering an expected amount'
    valid = false
  }
  return valid
}

function blockInvalidKeys(e) {
  if (['-', '+', 'e', 'E'].includes(e.key)) {
    e.preventDefault()
  }
}

async function submit() {
  if (!validate()) return
  loading.value = true
  apiError.value = ''

  const payload = {
    projectName:         form.projectName,
    targetGenre:         form.targetGenre,
    targetPlatform:      form.targetPlatform || null,
    expectedSalesRegion: (form.expectedSalesAmount && form.expectedSalesRegion) ? form.expectedSalesRegion : null,
    expectedSalesAmount: form.expectedSalesAmount !== '' ? form.expectedSalesAmount : null,
  }

  try {
    if (props.project) {
      await api.put(`/projects/${props.project.id}`, payload)
    } else {
      await api.post('/projects', payload)
    }
    emit('saved')
  } catch (e) {
    if (e.response?.status === 429) {
      apiError.value = 'Rate limit reached. Please try again later.'
    } else {
      apiError.value = 'Something went wrong. Please try again.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 1rem;
  backdrop-filter: blur(2px);
}

.modal {
  background-color: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 0;
}

.modal-title {
  font-size: 1.1rem;
  margin: 0;
  color: var(--color-text);
}

.modal-close {
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 1rem;
  padding: 0.25rem;
  line-height: 1;
  transition: color 0.15s;
}

.modal-close:hover { color: var(--color-text); }

.modal-body {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: 0.8rem;
  font-weight: 500;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.optional {
  text-transform: none;
  font-weight: 400;
  font-size: 0.75rem;
  opacity: 0.7;
}

.field-hint {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  opacity: 0.7;
}

.field-error {
  font-size: 0.775rem;
  color: #fca5a5;
}

.api-error {
  padding: 0.75rem 1rem;
  border-radius: 8px;
  background-color: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  color: #fca5a5;
  font-size: 0.875rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 0 1.5rem 1.5rem;
}

.custom-select {
  position: relative;
}

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
}

.btn-primary:hover:not(:disabled) { background-color: var(--color-accent-hover); }
.btn-primary:disabled { opacity: 0.4; cursor: not-allowed; }

.btn-secondary {
  padding: 0.5rem 1.25rem;
  border-radius: 8px;
  background-color: transparent;
  color: var(--color-text-muted);
  border: 1px solid var(--color-border);
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s;
}

.btn-secondary:hover {
  color: var(--color-text);
  border-color: var(--color-text-muted);
}
</style>