<template>
  <div>
    <Collapse :value="[1, 2, 3]">
      <Panel name="1">
        <span style="font-size: 12px">{{ $t('runtime_container') }}</span>
        <div slot="content" v-for="(item, index) in data.docker" :key="index">
          <pre>{{ JSON.stringify(item, null, 4) }}</pre>
        </div>
      </Panel>
      <Panel name="2">
        <span style="font-size: 12px">{{ $t('database') }}</span>
        <div slot="content" v-for="(item, index) in data.mysql" :key="index">
          <pre>{{ JSON.stringify(item, null, 4) }}</pre>
        </div>
      </Panel>
      <Panel name="3">
        <span style="font-size: 12px">{{ $t('storage_service') }}</span>
        <div slot="content" v-for="(item, index) in data.s3" :key="index">
          <pre>{{ JSON.stringify(item, null, 4) }}</pre>
        </div>
      </Panel>
    </Collapse>
  </div>
</template>

<script>
import { getRuntimeResource } from '@/api/server'
export default {
  name: 'runtime-resources',
  data () {
    return {
      data: {}
    }
  },
  watch: {
    pkgId: {
      handler: () => {
        this.getData()
      }
    }
  },
  props: {
    pkgId: {
      required: true
    }
  },
  created () {
    this.getData()
  },
  methods: {
    async getData () {
      let { status, data } = await getRuntimeResource(this.pkgId)
      if (status === 'OK') {
        this.data = data
      }
    }
  }
}
</script>
