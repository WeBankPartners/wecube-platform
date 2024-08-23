<template>
  <div id="wecube_app">
    <transition name="fade" mode="out-in">
      <router-view class="pages"></router-view>
    </transition>
  </div>
</template>
<script>
export default {
  mounted() {
    // remove loading
    const boxLoading = document.getElementById('boxLoading')
    const boxTitle = document.getElementById('boxTitle')
    boxLoading.style.display = 'none'
    boxTitle.style.display = 'none'
  }
}
</script>

<style lang="scss" scoped>
#wecube_app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100%;
  min-width: 1280px;
}
</style>
