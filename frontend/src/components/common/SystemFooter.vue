<template>
  
  <div v-if="hasInfo" class="system-footer">
    <span>{{ siteDescription }}</span>
    <span class="footer-dot">·</span>
    <span>电话 {{ contactPhone }}</span>
    <span class="footer-dot">·</span>
    <span>{{ contactEmail }}</span>
  </div>
</template>



<script setup lang="ts">


import { computed } from 'vue'
import { configStore } from '../../utils/configStore'

const siteDescription = computed(() => configStore.configs['site_description'] ?? '')
const contactPhone = computed(() => configStore.configs['contact_phone'] ?? '')
const contactEmail = computed(() => configStore.configs['contact_email'] ?? '')

const hasInfo = computed(() =>
  siteDescription.value || contactPhone.value || contactEmail.value
)
</script>
