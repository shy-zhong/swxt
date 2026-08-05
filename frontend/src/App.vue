<script setup lang="ts">



import { onMounted } from 'vue'
import { RouterView } from 'vue-router'
import SystemFooter from './components/common/SystemFooter.vue'
import { loadConfig } from './utils/configStore'

onMounted(() => {
  loadConfig()
})
</script>

<template>
  
  <RouterView />
  <SystemFooter />
</template>
