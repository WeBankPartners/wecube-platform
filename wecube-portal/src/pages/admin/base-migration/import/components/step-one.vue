<template>
  <div class="base-migration-import-one">
    <div v-if="!viewFlag" class="item">
      <span class="title">导入链接：</span>
      <div class="link">
        <Input v-model="url" placeholder="请输入导入链接" clearable style="width: 600px" />
        <Button type="primary" :disabled="!url" @click="getImportBusinessList">确认</Button>
      </div>
    </div>
    <Card :bordered="false" dis-hover :padding="0" style="min-height: 400px">
      <template v-if="detail.businessList.length > 0">
        <div class="item">
          <span class="title">选择环境：</span>
          <RadioGroup v-model="detail.environment.env_id" type="button" button-style="solid">
            <Radio v-for="(j, idx) in envList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
          </RadioGroup>
        </div>
        <div class="item">
          <span class="title">选择产品<span class="number">{{ detail.businessList.length }}</span></span>
          <Table :border="false" size="small" :columns="tableColumns" :max-height="500" :data="detail.businessList">
          </Table>
        </div>
      </template>
      <Spin v-if="loading" size="large" fix></Spin>
    </Card>
    <div class="footer">
      <Button v-if="viewFlag" type="info" @click="$emit('nextStep')">下一步</Button>
      <Button v-else type="info" @click="handleSave" :disabled="detail.businessList.length === 0">执行导入</Button>
    </div>
  </div>
</template>

<script>
import { getImportBusinessList, saveImportData } from '@/api/server'
import { debounce } from '@/const/util'
export default {
  props: {
    detailData: Object
  },
  data() {
    return {
      url: '',
      loading: false,
      detail: {
        exportNexusUrl: '',
        environment: {},
        businessList: []
      },
      viewFlag: false,
      envList: [],
      tableColumns: [
        {
          title: '业务产品名',
          minWidth: 180,
          key: 'name',
          render: (h, params) => (
            <span
              style="cursor:pointer;color:#5cadff;"
              onClick={() => {
                this.jumpToHistory(params.row)
              }}
            >
              {params.row.displayName}
            </span>
          )
        },
        {
          title: '产品ID',
          minWidth: 180,
          key: 'id'
        },
        {
          title: '产品描述',
          key: 'description',
          minWidth: 140,
          render: (h, params) => <span>{params.row.description || '-'}</span>
        },
        {
          title: this.$t('updatedBy'),
          key: 'update_user',
          minWidth: 120
        },
        {
          title: this.$t('table_updated_date'),
          key: 'update_time',
          minWidth: 150
        }
      ]
    }
  },
  mounted() {
    if (this.detailData && this.detailData.inputUrl) {
      this.url = this.detailData.inputUrl
      this.getImportBusinessList()
      this.viewFlag = true
    }
  },
  methods: {
    async getImportBusinessList() {
      this.loading = true
      const params = {
        params: {
          exportNexusUrl: this.url
        }
      }
      const { data, status } = await getImportBusinessList(params)
      this.loading = false
      if (status === 'OK') {
        this.detail = {
          exportNexusUrl: this.url,
          businessList: data.businessList || [],
          environment: data.environment || {}
        }
        this.envList = [
          {
            label: this.detail.environment.env_name,
            value: this.detail.environment.env_id
          }
        ]
      }
    },
    jumpToHistory() {},
    handleSave: debounce(async function () {
      const params = {
        exportNexusUrl: this.detail.exportNexusUrl,
        step: 2
      }
      const { data, status } = await saveImportData(params)
      if (status === 'OK') {
        // 执行导入，生成ID
        this.$emit('saveStepOne', data || '')
      }
    }, 500)
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-one {
  .item {
    display: flex;
    flex-direction: column;
    margin-bottom: 20px;
    padding-left: 12px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      font-weight: 600;
      .number {
        font-size: 18px;
        color: #2d8cf0;
        margin-left: 6px;
      }
    }
  }
  .footer {
    position: fixed;
    bottom: 10px;
    display: flex;
    justify-content: center;
    width: calc(100% - 460px);
  }
}
</style>
