<template>
  <div class="header-title">
    <div class="w-header">
      <Icon v-if="expand" size="26" type="md-arrow-dropdown" @click="handleExpand" style="cursor: pointer" />
      <Icon v-else size="26" type="md-arrow-dropright" @click="handleExpand" style="cursor: pointer" />
      <div class="title">{{ title }}<span class="underline"></span></div>
      <div class="header-slot">
        <slot name="header"></slot>
      </div>
    </div>
    <div v-show="expand" class="content">
      <slot></slot>
    </div>
    <div v-show="!expand" style="padding-bottom: 20px"></div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      expand: true
    }
  },
  methods: {
    handleExpand () {
      this.expand = !this.expand
    }
  }
}
</script>

<style lang="scss" scoped>
.header-title {
  .w-header {
    display: flex;
    align-items: center;
    .title {
      font-size: 15px;
      font-weight: 500;
      color: #282e38;
      margin: 0 10px;
      .underline {
        display: block;
        margin-top: -10px;
        margin-left: -6px;
        width: 100%;
        padding: 0 6px;
        height: 10px;
        border-radius: 12px;
        background-color: #c6eafe;
        box-sizing: content-box;
      }
    }
    .sub-title {
      font-size: 15px;
    }
    .header-slot {
      margin-left: 20px;
    }
  }
  .content {
    padding: 20px 10px;
  }
}
</style>
