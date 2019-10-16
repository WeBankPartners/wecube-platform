<template>
  <Row>
    <Col span="3">
      <RadioGroup v-model="vertical" vertical>
        <Radio label="apple">
          <Icon type="social-apple"></Icon>
          <span>Apple</span>
        </Radio>
        <Radio label="android">
          <Icon type="social-android"></Icon>
          <span>Android</span>
        </Radio>
        <Radio label="windows">
          <Icon type="social-windows"></Icon>
          <span>Windows</span>
        </Radio>
      </RadioGroup>
    </Col>
    <Col span="21"> </Col>
  </Row>
</template>
<script>
export default {
  data() {
    return {};
  },
  props: {},
  watch: {},
  methods: {}
};
</script>
<style lang="scss"></style>
