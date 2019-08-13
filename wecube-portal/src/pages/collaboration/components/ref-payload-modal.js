export default {
  name: "refPayloadModal",
  props: {
    ciRulesFilters: { default: () => [] }
  },
  data() {
    return {
      form: {},
      visibleSwap: false
    };
  },
  methods: {
    handleSubmit(ref) {
      this.$emit("handleSubmit", this.form);
      this.visibleSwap = false;
    },
    swapModal(status) {
      if (status) return;
      this.visibleSwap = false;
    },
    clickHandler() {
      this.visibleSwap = true;
    },

    getFormFilters() {
      return (
        <Form
          ref="form"
          value={this.form}
          onInput={v => (this.form = v)}
          label-position="top"
          inline
        >
          <Row>
            {this.ciRulesFilters.map(_ => {
              return this.renderFormItem(_);
            })}
            <Col span={3}>
              <FormItem style="position: relative; bottom: -22px;">
                <Button
                  type="primary"
                  icon="ios-search"
                  onClick={() => this.handleSubmit("form")}
                >
                  确认
                </Button>
              </FormItem>
            </Col>
          </Row>
        </Form>
      );
    },
    renderFormItem(item) {
      const data = {
        props: {
          ...item,
          ciType: {
            id: item.ciTypeId
          }
        },
        style: {
          width: "100%"
        }
      };

      let renders = item => {
        switch (item.inputType) {
          case "select":
            return (
              <WeSelect
                onInput={v => (this.form[item.ciTypeAttrId] = v)}
                value={this.form[item.ciTypeAttrId]}
                filterable
                clearable
                {...data}
                options={item.options}
              />
            );
          case "ref":
            return (
              <RefSelect
                onInput={v => (this.form[item.ciTypeAttrId] = v)}
                value={this.form[item.ciTypeAttrId]}
                {...data}
              />
            );
          default:
            return <div />;
        }
      };
      return (
        <Col span={item.span || 3}>
          <FormItem label={item.name} prop={item.name} key={item.name}>
            {renders(item)}
          </FormItem>
        </Col>
      );
    }
  },
  render(h) {
    return (
      <div>
        <Button onClick={this.clickHandler}>配置CI过滤规则</Button>
        <Modal
          title="CI过滤规则"
          value={this.visibleSwap}
          footer-hide={true}
          mask-closable={false}
          scrollable={true}
          width={70}
          on-on-visible-change={status => this.swapModal(status)}
        >
          {this.visibleSwap && <div>{this.getFormFilters()}</div>}
        </Modal>
      </div>
    );
  }
};
