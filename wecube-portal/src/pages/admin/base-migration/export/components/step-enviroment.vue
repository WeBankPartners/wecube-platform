<template>
  <div class="export-step-enviroment">
    <!--选择客户-->
    <div class="inline-item">
      <span class="title">{{ $t('pi_target_custom') }}</span>
      <Select
        v-model="customerId"
        @on-open-change="getCustomerList"
        clearable
        filterable
        :filter-by-label="true"
        style="width: 360px"
      >
        <Option v-for="i in customerList" :key="i.id" :label="i.name" :value="i.id"></Option>
      </Select>
      <Button type="primary" style="margin-left: 10px" @click="openCustomDialog">{{ $t('pi_custom_manage') }}</Button>
    </div>
    <!--选择环境-->
    <div class="inline-item">
      <span class="title">{{ $t('pe_select_env') }}</span>
      <RadioGroup v-model="env" type="button" button-style="solid">
        <Radio v-for="(j, idx) in envList" :label="j.value" :key="idx" border>{{ j.label }}</Radio>
      </RadioGroup>
    </div>
    <div class="inline-item">
      <div style="display: flex; align-items: center">
        <span class="title">{{ $t('pi_data_confirmTime') }}</span>
        <DatePicker
          type="datetime"
          format="yyyy-MM-dd HH:mm:ss"
          :value="lastConfirmTime"
          @on-change="
            val => {
              lastConfirmTime = val
            }
          "
          :placeholder="$t('tw_please_select')"
          style="width: 250px"
          clearable
        ></DatePicker>
        <span class="sub-title">*{{ $t('pi_data_confirmTimeTips') }}</span>
      </div>
    </div>
    <!--选择产品-->
    <div class="item">
      <span class="title">{{ $t('pe_select_product') }}<span class="number">{{ selectionList.length }}</span></span>
      <div>
        <ProductTree ref="productTree" :data="productData" @checkChange="handleProductSelect" />
      </div>
    </div>
    <!--选择区域-->
    <div class="item">
      <span class="title">{{ $t('pe_select_area') }}<span class="number">{{ deployZone.length }}</span></span>
      <Select
        v-model="deployZone"
        clearable
        multiple
        style="width: 600px"
      >
        <Option v-for="i in zoneList" :key="i.guid" :label="i.displayName" :value="i.guid"></Option>
      </Select>
    </div>
    <CustomManagement v-if="customVisible" v-model="customVisible" />
  </div>
</template>

<script>
import CustomManagement from './custom-management.vue'
import ProductTree from '../../components/product-tree.vue'
import { getExportBusinessList, getCustomerList, getExportZoneList } from '@/api/server'
import { pick } from 'lodash'
import dayjs from 'dayjs'
export default {
  components: { CustomManagement, ProductTree },
  props: {
    detailData: Object,
    from: String
  },
  data() {
    return {
      env: '', // 选择环境
      lastConfirmTime: dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'), // 数据确认时间
      envList: [],
      selectionList: [], // 勾选的二级产品
      loading: false,
      productData: [],
      customerId: '', // 目标客户
      customerList: [], // 客户列表
      customVisible: false,
      excludeDeployZone: [], // 排除的区域列表
      deployZone: [], // 选中的区域列表
      zoneList: []
    }
  },
  watch: {
    detailData: {
      handler(val) {
        if (val && val.environment) {
          this.env = val.environment
        }
      },
      immediate: true,
      deep: true
    }
  },
  async mounted() {
    await this.getProductList()
    await this.getEnviromentList()
    await this.getZoneList()
    if (this.detailData && this.detailData.environment) {
      this.env = this.detailData.environment
      // 获取产品数据
      this.productData = JSON.parse(this.detailData.selectedTreeJson || '[]')
      this.$nextTick(() => {
        const checkedTags = this.$refs.productTree.$refs.tree.getCheckedNodes() || []
        this.selectionList = checkedTags.filter(item => item.level === 2)
      })
      this.lastConfirmTime = this.detailData.lastConfirmTime
      // 目标客户
      this.customerId = this.detailData.customerId
      if (this.customerId) {
        this.getCustomerList()
      }
    }
  },
  methods: {
    // 获取环境列表
    async getEnviromentList() {
      const params = {
        queryMode: 'env' // env代表查询环境，空代表查询产品
      }
      const { status, data } = await getExportBusinessList(params)
      if (status === 'OK') {
        this.envList = data
          && data.map(item => ({
            label: item.displayName,
            value: item.id
          }))
        if (!this.env) {
          this.env = this.envList[0].value
        }
      }
    },
    handleProductSelect(selectionList, productData) {
      this.selectionList = selectionList
      this.productData = productData
    },
    // 获取产品列表
    async getProductList() {
      const params = {
        queryMode: '' // env代表查询环境，空代表查询产品
      }
      this.loading = true
      const { status, data } = await getExportBusinessList(params)
      this.loading = false
      if (status === 'OK') {
        this.productData = data || []    
        this.productData.forEach(x => {
          if (x.primary_products && x.primary_products.length > 0) {
            x.primary_products = x.primary_products.filter(y => {
              return y.secondary_products && y.secondary_products.length > 0
            })
          }
        })
        this.productData = this.productData.filter(x => {
          return x.primary_products && x.primary_products.length > 0
        })
        // 0级产品
        this.productData.forEach(x => {
          const disabled = x.primary_products.some(y => {
            return y.secondary_products && y.secondary_products.some(z => z.product_mandatory === 'true')
          })
          this.$set(x, 'title', x.name)
          this.$set(x, 'expand', false)
          this.$set(x, 'children', x.primary_products || [])
          if (!this.detailData.id) {
            this.$set(x, 'checked', true)
          } else {
            this.$set(x, 'checked', false)
          }
          this.$set(x, 'disabled', disabled)
          this.$set(x, 'level', 0)
          // 1级产品
          x.children.forEach(y => {
            const disabled = y.secondary_products &&y.secondary_products.some(z => z.product_mandatory === 'true')
            this.$set(y, 'title', y.name)
            this.$set(y, 'expand', false)
            this.$set(y, 'children', y.secondary_products || [])
            if (!this.detailData.id) {
              this.$set(y, 'checked', true)
            } else {
              this.$set(y, 'checked', false)
            }
            this.$set(y, 'disabled', disabled)
            this.$set(y, 'level', 1)
            // 2级产品
            y.children.forEach(z => {
              // 3级显示系统，禁用勾选
              z.system_design = z.system_design && z.system_design.map(sys => {
                return {
                  title: sys,
                  expand: false,
                  disableCheckbox: true
                }
              }) || []
              this.$set(z, 'title', `${z.name}`)
              this.$set(z, 'expand', false)
              this.$set(z, 'children', z.system_design || [])
              if (!this.detailData.id) {
                this.$set(z, 'checked', true)
              } else {
                this.$set(z, 'checked', false)
              }
              this.$set(z, 'disabled', z.product_mandatory === 'true' ? true : false)
              this.$set(z, 'level', 2)
            })
          })
        })
        // 过滤产品树数据属性，只保留以下属性
        const pickAttrs = ['title', 'expand', 'children', 'checked', 'disabled', 'level', 'id', 'nodeKey', 'code', 'displayName']
        this.productData.forEach(x => {
          x.children.forEach(y => {
            y.children = y.children.map(z => {
              return pick(z, pickAttrs)
            })
          })
        })
        this.productData.forEach(x => {
          x.children = x.children.map(y => {
            return pick(y, pickAttrs)
          })
        })
        this.productData = this.productData.map(x => {         
            return pick(x, pickAttrs)
        })
        if (!this.detailData.id) {
          this.$nextTick(() => {
            const checkedTags = this.$refs.productTree.$refs.tree.getCheckedNodes() || []
            this.selectionList = checkedTags.filter(item => item.level === 2)
          })
        }
      }
    },
    jumpToHistory() {},
    // 打开目标客户弹窗
    openCustomDialog() {
      this.customVisible = true
    },
    // 获取目标客户列表
    async getCustomerList() {
      const { status, data } = await getCustomerList()
      if (status === 'OK') {
        this.customerList = data || []
      }
    },
    async getZoneList() {
      const { status, data } = await getExportZoneList()
      if (status === 'OK') {
        this.zoneList = data || []
        this.zoneList = this.zoneList.map(item => {
          item.displayName = `${item.deploy_zone_design.key_name}/${item.name}`
          this.deployZone.push(item.guid)
          return item
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.export-step-enviroment {
  .inline-item {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding-left: 12px;
    .title {
      font-size: 14px;
      margin-bottom: 5px;
      margin-right: 10px;
      font-weight: 600;
      width: fit-content;
      min-width: 90px;
      .number {
        font-size: 18px;
        color: #5384ff;
        margin-left: 6px;
      }
    }
    .sub-title {
      font-size: 14px;
      font-weight: normal;
      color: #ff4d4f;
      margin-left: 5px;
    }
  }
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
        color: #5384ff;
        margin-left: 6px;
      }
    }
  }
}
</style>
<style lang="scss">
.export-step-enviroment {
  .common-base-search-button {
    width: fit-content;
  }
}
</style>
