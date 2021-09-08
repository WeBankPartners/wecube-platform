<template>
  <div class="">
    <div class="report-container">
      <div class="item">
        {{ $t('datetime_range') }}:
        <DatePicker type="datetimerange" format="yyyy-MM-dd HH:mm"></DatePicker>
      </div>
      <div class="item">
        {{ $t('flow_name') }}:
        <Select v-model="model1" multiple filterable clearable style="width:200px">
          <Option v-for="item in cityList" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>
      <div class="item">
        {{ $t('task_node') }}:
        <Select v-model="model1" multiple filterable clearable style="width:200px">
          <Option v-for="item in cityList" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>
      <div class="item">
        {{ $t('task_node_bindings') }}:
        <Select v-model="model1" multiple filterable clearable style="width:200px">
          <Option v-for="item in cityList" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>
      <div class="item">
        <Button type="primary">{{ $t('query') }}</Button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: '',
  data () {
    return {
      searchParams: {},
      model1: '',
      cityList: [
        {
          value: 'New York',
          label: 'New York'
        }
      ]
    }
  },
  methods: {},
  components: {}
}
</script>

<style scoped lang="scss">
.report-container {
  display: flex;
  flex-wrap: wrap;
}
.item {
  width: 300px;
  margin: 8px;
}
</style>
