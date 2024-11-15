<!--
 * @Author: wanghao7717 792974788@qq.com
 * @Date: 2024-10-16 15:32:21
 * @LastEditors: wanghao7717 792974788@qq.com
 * @LastEditTime: 2024-11-14 16:37:23
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
        <template v-if="['wecubeHostPassword', 'wecubeHost1Password', 'wecubeHost2Password'].includes(i.key)">
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
        <Upload v-if="idx === 0" action="#" :before-upload="uploadJsonFile" style="display: inline-block">
          <Button type="primary" :disabled="getDisabled">{{ $t('pe_one_import') }}</Button>
        </Upload>
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
        dataCenterRegionAssetId: '',
        dataCenterAZ1AssetId: '',
        dataCenterAZ2AssetId: '',
        networkZoneAssetId: '',
        networkSubZone1AssetId: '',
        networkSubZone2AssetId: '',
        routeTableAssetId: '',
        wecubeHost1AssetId: '',
        wecubeHost1Password: '',
        wecubeHost2AssetId: '',
        wecubeHost2Password: ''
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
          label: '网络子区域1 MGMT_APP-资产ID',
          key: 'networkSubZone1AssetId'
        },
        {
          label: '网络子区域2 MGMT_APP-资产ID',
          key: 'networkSubZone2AssetId'
        },
        {
          label: '路由表 默认路由表-资产ID',
          key: 'routeTableAssetId'
        },
        {
          label: '主机资源 wecube主机1-资产ID',
          key: 'wecubeHost1AssetId'
        },
        {
          label: '主机资源 wecube主机1-管理员密码',
          key: 'wecubeHost1Password'
        },
        {
          label: '主机资源 wecube主机2-资产ID',
          key: 'wecubeHost2AssetId'
        },
        {
          label: '主机资源 wecube主机2-管理员密码',
          key: 'wecubeHost2Password'
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
      this.importCustomFormData = Object.assign({}, this.importCustomFormData, this.detailData.modifyNewEnvDataRes.data)
    }
  },
  methods: {
    /**
     * 在文件上传前进行校验
     * @param file 上传的文件对象
     * @returns 如果文件类型为 JSON，则返回 true；否则返回 false，并显示错误信息
     */
    uploadJsonFile(file) {
      const isJSON = file.type === 'application/json'
      if (!isJSON) {
        this.$Message.error('只能上传 JSON 文件!')
        return
      }
      const reader = new FileReader()
      reader.readAsText(file)
      reader.onload = e => {
        try {
          const jsonData = JSON.parse(e.target.result)
          const keys = Object.keys(this.importCustomFormData)
          for (const key of keys) {
            if (jsonData[key]) {
              this.importCustomFormData[key] = jsonData[key]
            }
          }
        } catch (error) {
          console.error('json error', error)
        }
      }
    },
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
