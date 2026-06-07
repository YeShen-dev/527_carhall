<template>
  <div class="filter-bar">
    <div class="filter-row">
      <div class="filter-item">
        <span class="filter-label"><el-icon><Menu /></el-icon> 分类</span>
        <el-select
          :model-value="category"
          clearable
          placeholder="全部分类"
          size="large"
          @update:model-value="$emit('update:category', $event)"
          @change="$emit('search')"
        >
          <el-option
            v-for="cat in categories"
            :key="cat"
            :label="cat"
            :value="cat"
          />
        </el-select>
      </div>

      <div class="filter-item">
        <span class="filter-label"><el-icon><Search /></el-icon> 品牌</span>
        <el-input
          :model-value="brand"
          placeholder="输入品牌名称..."
          clearable
          size="large"
          @update:model-value="$emit('update:brand', $event)"
          @keyup.enter="$emit('search')"
          @clear="$emit('search')"
        />
      </div>

      <div class="filter-item">
        <span class="filter-label"><el-icon><Sort /></el-icon> 排序</span>
        <el-select
          :model-value="sort"
          size="large"
          @update:model-value="$emit('update:sort', $event)"
          @change="$emit('search')"
        >
          <el-option label="默认排序" value="default" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
      </div>

      <el-button type="danger" size="large" @click="$emit('search')">
        <el-icon><Search /></el-icon> 搜索
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  category: String,
  brand: String,
  sort: String,
  categories: Array
})

defineEmits(['update:category', 'update:brand', 'update:sort', 'search'])
</script>

<style scoped>
.filter-bar {
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  border: 1px solid #F1F5F9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
}
.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-label {
  font-size: 13px;
  color: #64748B;
  white-space: nowrap;
  display: flex;
  align-items: center;
  gap: 4px;
}
.filter-item .el-select,
.filter-item .el-input {
  width: 170px;
}
</style>
