<template>

  <div class="product-management">
    <div class="page-header">
      <h2>出入库记录</h2>
      <button class="back-btn" @click="router.push('/operator/home')">返回</button>
    </div>

    <StockRecordTable />
  </div>
</template>

<script setup lang="ts">
import router from '../../route/Router'
import StockRecordTable from '../common/StockRecordTable.vue'
</script>
