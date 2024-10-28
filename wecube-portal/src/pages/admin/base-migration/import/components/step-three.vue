<!--
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2024-10-16 15:32:21
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2024-10-28 10:17:24
-->
<template>
  <div class="base-migration-import-three">
    <Form ref="form" :model="importCustomFormData" :label-width="250">
      <FormItem
        v-for="(i, idx) in options"
        :key="idx"
        :label="i.label"
        :prop="i.key"
        :rules="{
          required: ['dataCenterRegionAssetId', 'dataCenterAZ1AssetId', 'dataCenterAZ2AssetId'].includes(i.key)
            ? true
            : false,
          message: '不能为空',
          trigger: 'blur'
        }"
      >
        <template v-if="i.key === 'wecubeHostPassword'">
          <input type="text" style="display: none" />
          <input type="password" autocomplete="new-password" style="display: none" />
          <Input
            v-model="importCustomFormData[i.key]"
            type="password"
            autocomplete="off"
            password
            :maxlength="100"
            :disabled="getDisabled"
            placeholder="请输入"
            style="width: 500px"
          />
        </template>
        <Input
          v-else
          v-model="importCustomFormData[i.key]"
          autocomplete="off"
          :maxlength="100"
          clearable
          :disabled="getDisabled"
          placeholder="请输入"
          style="width: 500px"
        />
      </FormItem>
    </Form>
    <div class="footer">
      <Button type="default" @click="handleLast">{{ $t('privious_step') }}</Button>
      <Button type="primary" @click="handleNext">{{ $t('next_step') }}</Button>
    </div>
  </div>
</template>

<script>
import { saveImportData } from '@/api/server'
import { debounce } from '@/const/util'
export default {
  props: {
    detailData: Object
  },
  data() {
    return {
      importCustomFormData: {
        networkZoneAssetId: '',
        networkSubZoneAssetId: '',
        routeTableAssetId: '',
        basicSecurityGroupAssetId: '',
        dataCenterRegionAssetId: '',
        dataCenterAZ1AssetId: '',
        dataCenterAZ2AssetId: '',
        wecubeHostAssetId: '',
        wecubeHostPassword: ''
      },
      options: [
        {
          label: '数据中心 地域数据中心-资产ID',
          key: 'dataCenterRegionAssetId'
        },
        {
          label: '地域数据中心可用区1-资产ID',
          key: 'dataCenterAZ1AssetId'
        },
        {
          label: '地域数据中心可用区2-资产ID',
          key: 'dataCenterAZ2AssetId'
        },
        {
          label: '网络区域-资产ID',
          key: 'networkZoneAssetId'
        },
        {
          label: '网络子区域 MGMT_APP-资产ID',
          key: 'networkSubZoneAssetId'
        },
        {
          label: '路由表 默认路由表-资产ID',
          key: 'routeTableAssetId'
        },
        {
          label: '基础安全组 MGMT-APP-资产ID',
          key: 'basicSecurityGroupAssetId'
        },
        {
          label: '主机资源 wecube主机的-资产ID',
          key: 'wecubeHostAssetId'
        },
        {
          label: '主机资源 wecube主机的-管理员密码',
          key: 'wecubeHostPassword'
        }
      ]
    }
  },
  computed: {
    getDisabled() {
      return this.detailData.step > 3 || this.detailData.status === 'success'
    }
  },
  mounted() {
    if (this.detailData && this.detailData.modifyNewEnvDataRes) {
      this.importCustomFormData = this.detailData.modifyNewEnvDataRes.data
    }
  },
  methods: {
    handleNext: debounce(async function () {
      if (this.detailData.step > 3 || this.detailData.status === 'success') {
        this.$emit('nextStep')
      } else {
        this.$refs.form.validate(async valid => {
          if (valid) {
            const params = {
              transImportId: this.detailData.id,
              step: 3,
              importCustomFormData: this.importCustomFormData
            }
            const { status } = await saveImportData(params)
            if (status === 'OK') {
              // 执行导入，生成ID
              this.$emit('saveStepThree')
            }
          }
        })
      }
    }, 500),
    // 上一步
    handleLast() {
      this.$emit('lastStep')
    }
  }
}
</script>

<style lang="scss" scoped>
.base-migration-import-three {
  .footer {
    position: fixed;
    bottom: 10px;
    display: flex;
    justify-content: center;
    width: calc(100% - 460px);
    button {
      &:not(:first-child) {
        margin-left: 10px;
      }
    }
  }
}
</style>
