<template>
  <div v-if="visible" class="panel-overlay" @click.self="emit('close')">
    <div class="edit-panel">
      <h3>
        <slot name="title">{{ title }}</slot>
      </h3>
      <slot />
      <div v-if="$slots.actions" class="form-actions modal-actions">
        <slot name="actions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{ visible: boolean; title?: string }>(), { title: '' })

const emit = defineEmits<{ (e: 'close'): void }>()
</script>
