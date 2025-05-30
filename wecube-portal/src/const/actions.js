export const components = {
  number: {
    component: 'Input',
    type: 'number'
  },
  date: {
    component: 'DatePicker',
    type: 'datetimerange'
  },
  text: {
    component: 'Input',
    type: 'text'
  },
  select: {
    component: 'WeSelect',
    options: []
  },
  ref: {
    component: 'refSelect',
    highlightRow: true
  },
  multiSelect: {
    component: 'WeSelect',
    options: []
  },
  multiRef: {
    component: 'refSelect'
  },
  textArea: {
    component: 'Input',
    type: 'text'
  }
}
export const outerActions = [
  {
    label: window.vm.$i18n.t('add'),
    props: {
      type: 'success',
      icon: 'md-add',
      disabled: false
    },
    actionType: 'add'
  },
  {
    label: window.vm.$i18n.t('save'),
    props: {
      type: 'info',
      icon: 'md-checkmark',
      disabled: true
    },
    actionType: 'save'
  },
  {
    label: window.vm.$i18n.t('edit'),
    props: {
      type: 'info',
      icon: 'ios-build',
      disabled: true
    },
    actionType: 'edit'
  },
  {
    label: window.vm.$i18n.t('delete'),
    props: {
      type: 'error',
      icon: 'ios-trash-outline',
      disabled: true
    },
    actionType: 'delete'
  },
  {
    label: window.vm.$i18n.t('cancel'),
    props: {
      type: 'warning',
      icon: 'md-undo'
    },
    actionType: 'cancel'
  },
  {
    label: window.vm.$i18n.t('export'),
    props: {
      type: 'primary',
      icon: 'ios-download-outline'
    },
    actionType: 'export'
  },
  {
    label: window.vm.$i18n.t('filter_columns'),
    props: {
      type: 'primary',
      icon: 'ios-funnel',
      shape: 'circle',
      disabled: false
    },
    actionType: 'filterColumns'
  }
]
export const innerActions = [
  {
    label: window.vm.$i18n.t('cancel'),
    props: {
      type: 'warning',
      size: 'small'
    },
    actionType: 'innerCancel',
    visible: {
      key: 'isRowEditable',
      value: true
    }
  }
]
export const pagination = {
  pageSize: 10,
  currentPage: 1,
  total: 0
}
