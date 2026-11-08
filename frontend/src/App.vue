<template>
  <div class="min-h-screen" style="background-color: var(--color-bg)">
    <NavBar />
    <main class="max-w-7xl mx-auto px-4 py-8">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
</script>