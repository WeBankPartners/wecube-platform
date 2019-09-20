<template>
  <div id="spa-plugin-container" v-if="isSPA">
    SPA test
  </div>
  <iframe id="iframe-plugin-container" v-else>
    iframe test
  </iframe>
</template>

<script>
export default {
  data() {
    return {
      isSPA: true
    };
  },
  methods: {},
  mounted() {
    console.log("route", this.$route);
  }
};
</script>
