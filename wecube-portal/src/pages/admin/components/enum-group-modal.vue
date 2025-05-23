<template>
  <Modal
    width="1000"
    :mask-closable="false"
    @on-visible-change="visibleChangeHandler"
    :footer-hide="true"
    v-model="isVisible"
    :title="$t('enum_management')"
  >
    <div style="border-bottom: 1px solid grey">
      <Form>
        <Row>
          <Col span="5">
            <FormItem :label-width="50" :label="$t('name')">
              <Input v-model="form.catName"></Input>
            </FormItem>
          </Col>
          <Col offset="1" span="6">
            <FormItem :label-width="90" :label="$t('enum_type')">
              <Select filterable :disabled="!!category" v-model="form.catTypeId">
                <Option v-for="item in catTypes" :value="item.value" :key="item.value">{{ item.label }}</Option>
              </Select>
            </FormItem>
          </Col>
          <Col offset="1" span="5">
            <FormItem :label-width="50" :label="$t('enum_group')">
              <Select filterable clearable v-model="form.catGroupId">
                <Option v-for="item in allCategory" :value="item.catId" :key="item.catId">{{ item.catName }}</Option>
              </Select>
            </FormItem>
          </Col>
          <Col offset="1" span="5">
            <Button type="primary" @click="saveCategoryHandler">{{ $t('save') }}</Button>
          </Col>
        </Row>
      </Form>
    </div>
    <div class="modalTable" style="padding: 40px 10px">
      <baseData v-if="categoryId > -1" ref="enumModal" :catId="categoryId"></baseData>
    </div>
  </Modal>
</template>
<script>
import baseData from '../enums'
import { createEnumCategory, updateEnumCategory } from '@/api/server'
export default {
  components: {
    baseData
  },
  data() {
    return {
      form: {
        catName: '',
        catTypeId: '',
        catGroupId: ''
      },
      categoryId: -1,
      catTypes: [
        {
          label: this.$t('pub_enum'),
          value: 2
        },
        {
          label: this.$t('pri_enum'),
          value: 3
        }
      ],
      isVisible: this.enumGroupVisible
    }
  },
  watch: {
    enumGroupVisible(val) {
      this.isVisible = val
    },
    category: {
      handler(val) {
        if (val) {
          this.form = {
            ...this.form,
            ...val,
            catTypeId: val.catTypeId === 2 ? val.catTypeId : 3,
            catGroupId: val.groupTypeId
          }
          this.categoryId = val.catId
        } else {
          this.form = {
            catName: '',
            catTypeId: '',
            catGroupId: ''
          }
          this.categoryId = -1
        }
      }
    }
  },
  props: {
    enumGroupVisible: {},
    allEnumCategoryTypes: {},
    currentCiType: {},
    category: {},
    allCategory: {}
  },
  computed: {},
  methods: {
    async saveCategoryHandler() {
      const type = this.allEnumCategoryTypes.find(_ => _.ciTypeId === this.currentCiType.ciTypeId)
      if (this.category) {
        // update
        const payload = {
          catId: this.form.catId,
          catName: this.form.catName,
          catTypeId: this.form.catTypeId,
          groupTypeId: this.form.catGroupId
        }
        const { message, status } = await updateEnumCategory(payload)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'update category Success',
            desc: message
          })
          this.$refs.enumModal.$refs.table.form.catId = this.categoryId
          this.$refs.enumModal.getGroupList(this.categoryId)
        }
      } else {
        // create
        const payload = {
          catName: this.form.catName,
          catTypeId: this.form.catTypeId === 2 ? this.form.catTypeId : type.catTypeId,
          groupTypeId: this.form.catGroupId
        }
        const { message, data, status } = await createEnumCategory(payload)
        if (status === 'OK') {
          this.$Notice.success({
            title: 'Add category Success',
            desc: message
          })
          this.categoryId = data[0].catId
          this.$nextTick(() => {
            this.$refs.enumModal.$refs.table.form.catId = this.categoryId
            this.$refs.enumModal.getGroupList(this.categoryId)
          })
          this.$emit('getAllEnums')
        }
      }
    },
    visibleChangeHandler(status) {
      if (!status) {
        this.$emit('hideHandler')
        this.form = {
          catName: '',
          catTypeId: '',
          catGroupId: ''
        }
        this.categoryId = -1
      }
      if (status && this.categoryId > 0) {
        this.$nextTick(() => {
          this.$refs.enumModal.$refs.table.form.catId = this.categoryId
        })
      }
    }
  }
}
</script>
