(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-6398251e"],{"20f6":function(t,e,n){},"77d4":function(t,e,n){},"7f79":function(t,e,n){"use strict";n.r(e);var r=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",[t.experiment?[n("div",{staticClass:"experiment-steps"},[n("aside",{staticClass:"experiment-steps__sidebar"},[n("steps",{attrs:{"current-section":t.currentSection,"current-step":t.currentStep}})],1),n("nav",[t.$routerHistory.hasPrevious()?n("router-link",{attrs:{to:{path:t.$routerHistory.previous().path}}},[n("v-icon",[t._v("mdi-chevron-left")]),t._v(" Back ")],1):t._e()],1),n("article",{staticClass:"experiment-steps__body"},[n("v-container",[n("v-row",{attrs:{justify:"center"}},[n("v-col",{attrs:{md:"6"}},[n("router-view",{key:t.$route.fullPath,attrs:{experiment:t.experiment}})],1)],1)],1)],1)])]:[n("v-row",{attrs:{justify:"center"}},[n("v-col",{attrs:{md:"6"}},[n("v-alert",{attrs:{prominent:"",type:"error"}},[n("v-row",{attrs:{align:"center"}},[n("v-col",{staticClass:"grow"},[t._v(" Experiment not found ")])],1)],1)],1)],1)]],2)},i=[],s=n("5530"),a=function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("ul",{staticClass:"component-steps"},t._l(t.sectionList,(function(e){return n("li",{key:e.key},[e.key===t.currentSection?[n("strong",{class:{"green--text":t.isSummary&&e.key===t.$route.meta.currentSection}},[t._v(t._s(e.name))])]:[n("span",{class:{"green--text":t.isSummary&&e.key===t.$route.meta.currentSection}},[t._v(t._s(e.name))])],e.key===t.currentSection?n("v-stepper",{class:{finished:t.isSummary},attrs:{vertical:""}},t._l(e.steps,(function(r){return n("v-stepper-step",{key:r.key,attrs:{complete:e.steps.findIndex((function(t){return t.key===r.key}))<=t.currentStepNum,step:""}},[t._v(" "+t._s(r.name)+" ")])})),1):t._e()],2)})),0)},o=[],c=(n("b0c0"),n("c740"),n("4de4"),{name:"Steps",props:["currentSection","currentStep"],computed:{isSummary:function(){return"ExperimentDesignSummary"===this.$route.name},currentStepNum:function(){var t=this;return this.sectionList.filter((function(e){return e.key===t.currentSection}))[0].steps.findIndex((function(e){return e.key===t.currentStep}))}},data:function(){return{sectionList:[{key:"design",name:"Section 1: Design",steps:[{key:"design_title",name:"Title"},{key:"design_description",name:"Description"},{key:"design_conditions",name:"Conditions"},{key:"design_type",name:"Experiment type"}]},{key:"participation",name:"Section 2: Participation",steps:[{key:"participation_selection_method",name:"Selection Method"}]},{key:"assignments",name:"Section 3: Assignments",steps:[{key:"design_title",name:"Title"}]}]}}}),p=c,u=(n("f8b9"),n("2877")),l=n("6544"),d=n.n(l),h=(n("a9e3"),n("8836"),n("3206")),f=n("a452"),m=n("7560"),v=n("58df"),g=n("d9bd"),y=Object(v["a"])(Object(h["b"])("stepper"),f["a"],m["a"]),b=y.extend({name:"v-stepper",provide:function(){return{stepClick:this.stepClick,isVertical:this.vertical}},props:{altLabels:Boolean,nonLinear:Boolean,vertical:Boolean},data:function(){var t={isBooted:!1,steps:[],content:[],isReverse:!1};return t.internalLazyValue=null!=this.value?this.value:(t[0]||{}).step||1,t},computed:{classes:function(){return Object(s["a"])({"v-stepper--is-booted":this.isBooted,"v-stepper--vertical":this.vertical,"v-stepper--alt-labels":this.altLabels,"v-stepper--non-linear":this.nonLinear},this.themeClasses)}},watch:{internalValue:function(t,e){this.isReverse=Number(t)<Number(e),e&&(this.isBooted=!0),this.updateView()}},created:function(){this.$listeners.input&&Object(g["a"])("@input","@change",this)},mounted:function(){this.updateView()},methods:{register:function(t){"v-stepper-step"===t.$options.name?this.steps.push(t):"v-stepper-content"===t.$options.name&&(t.isVertical=this.vertical,this.content.push(t))},unregister:function(t){"v-stepper-step"===t.$options.name?this.steps=this.steps.filter((function(e){return e!==t})):"v-stepper-content"===t.$options.name&&(t.isVertical=this.vertical,this.content=this.content.filter((function(e){return e!==t})))},stepClick:function(t){var e=this;this.$nextTick((function(){return e.internalValue=t}))},updateView:function(){for(var t=this.steps.length;--t>=0;)this.steps[t].toggle(this.internalValue);for(var e=this.content.length;--e>=0;)this.content[e].toggle(this.internalValue,this.isReverse)}},render:function(t){return t("div",{staticClass:"v-stepper",class:this.classes},this.$slots.default)}}),_=(n("d3b7"),n("25f0"),n("9d26")),S=n("a9ad"),k=n("5607"),x=Object(v["a"])(S["a"],Object(h["a"])("stepper","v-stepper-step","v-stepper")),$=x.extend().extend({name:"v-stepper-step",directives:{ripple:k["a"]},inject:["stepClick"],props:{color:{type:String,default:"primary"},complete:Boolean,completeIcon:{type:String,default:"$complete"},editable:Boolean,editIcon:{type:String,default:"$edit"},errorIcon:{type:String,default:"$error"},rules:{type:Array,default:function(){return[]}},step:[Number,String]},data:function(){return{isActive:!1,isInactive:!0}},computed:{classes:function(){return{"v-stepper__step--active":this.isActive,"v-stepper__step--editable":this.editable,"v-stepper__step--inactive":this.isInactive,"v-stepper__step--error error--text":this.hasError,"v-stepper__step--complete":this.complete}},hasError:function(){return this.rules.some((function(t){return!0!==t()}))}},mounted:function(){this.stepper&&this.stepper.register(this)},beforeDestroy:function(){this.stepper&&this.stepper.unregister(this)},methods:{click:function(t){t.stopPropagation(),this.$emit("click",t),this.editable&&this.stepClick(this.step)},genIcon:function(t){return this.$createElement(_["a"],t)},genLabel:function(){return this.$createElement("div",{staticClass:"v-stepper__label"},this.$slots.default)},genStep:function(){var t=!(this.hasError||!this.complete&&!this.isActive)&&this.color;return this.$createElement("span",this.setBackgroundColor(t,{staticClass:"v-stepper__step__step"}),this.genStepContent())},genStepContent:function(){var t=[];return this.hasError?t.push(this.genIcon(this.errorIcon)):this.complete?this.editable?t.push(this.genIcon(this.editIcon)):t.push(this.genIcon(this.completeIcon)):t.push(String(this.step)),t},toggle:function(t){this.isActive=t.toString()===this.step.toString(),this.isInactive=Number(t)<Number(this.step)}},render:function(t){return t("div",{staticClass:"v-stepper__step",class:this.classes,directives:[{name:"ripple",value:this.editable}],on:{click:this.click}},[this.genStep(),this.genLabel()])}}),C=Object(u["a"])(p,a,o,!1,null,null,null),V=C.exports;d()(C,{VStepper:b,VStepperStep:$});var I=n("4360"),j=n("2f62"),E={name:"ExperimentSteps",data:function(){return{}},computed:{currentSection:function(){return this.$router.currentRoute.meta.currentSection},currentStep:function(){return this.$router.currentRoute.meta.currentStep},experiment:function(){return this.$store.state.experiment.experiment},routeExperimentId:function(){return this.$route.params.experiment_id}},beforeRouteEnter:function(t,e,n){return I["a"].dispatch("experiment/fetchExperimentById",t.params.experiment_id).then(n,n)},beforeRouteUpdate:function(t,e,n){return I["a"].dispatch("experiment/fetchExperimentById",t.params.experiment_id).then(n,n)},methods:Object(s["a"])({},Object(j["b"])({fetchExperimentById:"experiment/fetchExperimentById"})),components:{Steps:V}},w=E,B=(n("ff59"),n("0798")),L=n("62ad"),O=n("a523"),P=n("132d"),A=n("0fd9"),R=Object(u["a"])(w,r,i,!1,null,"66577dde",null);e["default"]=R.exports;d()(R,{VAlert:B["a"],VCol:L["a"],VContainer:O["a"],VIcon:P["a"],VRow:A["a"]})},8836:function(t,e,n){},a452:function(t,e,n){"use strict";var r=n("ade3"),i=n("2b0e");function s(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"value",e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:"change";return i["a"].extend({name:"proxyable",model:{prop:t,event:e},props:Object(r["a"])({},t,{required:!1}),data:function(){return{internalLazyValue:this[t]}},computed:{internalValue:{get:function(){return this.internalLazyValue},set:function(t){t!==this.internalLazyValue&&(this.internalLazyValue=t,this.$emit(e,t))}}},watch:Object(r["a"])({},t,(function(t){this.internalLazyValue=t}))})}var a=s();e["a"]=a},a523:function(t,e,n){"use strict";n("4de4"),n("b64b"),n("2ca0"),n("99af"),n("20f6"),n("4b85"),n("498a"),n("a15b");var r=n("2b0e");function i(t){return r["a"].extend({name:"v-".concat(t),functional:!0,props:{id:String,tag:{type:String,default:"div"}},render:function(e,n){var r=n.props,i=n.data,s=n.children;i.staticClass="".concat(t," ").concat(i.staticClass||"").trim();var a=i.attrs;if(a){i.attrs={};var o=Object.keys(a).filter((function(t){if("slot"===t)return!1;var e=a[t];return t.startsWith("data-")?(i.attrs[t]=e,!1):e||"string"===typeof e}));o.length&&(i.staticClass+=" ".concat(o.join(" ")))}return r.id&&(i.domProps=i.domProps||{},i.domProps.id=r.id),e(r.tag,i,s)}})}var s=n("d9f7");e["a"]=i("container").extend({name:"v-container",functional:!0,props:{id:String,tag:{type:String,default:"div"},fluid:{type:Boolean,default:!1}},render:function(t,e){var n,r=e.props,i=e.data,a=e.children,o=i.attrs;return o&&(i.attrs={},n=Object.keys(o).filter((function(t){if("slot"===t)return!1;var e=o[t];return t.startsWith("data-")?(i.attrs[t]=e,!1):e||"string"===typeof e}))),r.id&&(i.domProps=i.domProps||{},i.domProps.id=r.id),t(r.tag,Object(s["a"])(i,{staticClass:"container",class:Array({"container--fluid":r.fluid}).concat(n||[])}),a)}})},abaf:function(t,e,n){},f8b9:function(t,e,n){"use strict";n("abaf")},ff59:function(t,e,n){"use strict";n("77d4")}}]);
//# sourceMappingURL=chunk-6398251e.83afcc87.js.map