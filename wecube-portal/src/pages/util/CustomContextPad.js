export default class CustomContextPad {
  constructor (config, contextPad, create, elementFactory, injector, translate, modeling, bpmnFactory) {
    this.create = create
    this.elementFactory = elementFactory
    this.translate = translate
    this.modeling = modeling
    this.bpmnFactory = bpmnFactory

    if (config.autoPlace !== false) {
      this.autoPlace = injector.get('autoPlace', false)
    }

    contextPad.registerProvider(this) // // 定义这是一个contextPad
  }

  getContextPadEntries (element) {
    const { autoPlace, create, elementFactory, translate } = this

    function appendTask (event, element) {
      console.log(autoPlace)
      if (autoPlace) {
        const shape = elementFactory.createShape({ type: 'bpmn:SubProcess' })
        autoPlace.append(element, shape)
      } else {
        appendTaskStart(event, element)
      }
    }

    function appendTaskStart (event) {
      const shape = elementFactory.createShape({ type: 'bpmn:SubProcess' })
      create.start(event, shape, element)
    }

    return {
      'append.subprocess-task': {
        group: 'model',
        className: 'entry bpmn-icon-subprocess-collapsed',
        title: translate('Sub Process (collapsed)'),
        action: {
          click: appendTask,
          dragstart: appendTaskStart
        }
      }
    }
  }
}

CustomContextPad.$inject = [
  'config',
  'contextPad',
  'create',
  'elementFactory',
  'injector',
  'translate',
  'modeling',
  'bpmnFactory'
]
