(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-438edfb2"],{"6fac":function(e,n,t){},bec2:function(e,n,t){"use strict";t.r(n);var a=function(){var e=this,n=e.$createElement,t=e._self._c||n;return t("div",[e._m(0),e.experiment?[this.experiment.participationType?t("v-expansion-panels",{attrs:{flat:""}},[t("v-expansion-panel",{staticClass:"py-3 mb-3"},[t("v-expansion-panel-header",[t("strong",[e._v("Selection Method")])]),t("v-expansion-panel-content",[t("p",[e._v(e._s(e.participationType))])])],1)],1):e._e(),this.consent&&this.consent.title?t("v-expansion-panels",{attrs:{flat:""}},[t("v-expansion-panel",{staticClass:"py-3 mb-3"},[t("v-expansion-panel-header",[t("strong",[e._v("Assignment Title")])]),t("v-expansion-panel-content",[t("p",[e._v(e._s(e.consent.title))])])],1)],1):e._e()]:e._e(),t("v-btn",{staticClass:"mt-3",attrs:{elevation:"0",color:"primary"},on:{click:e.nextSection}},[e._v(" Continue to next section ")])],2)},s=[function(){var e=this,n=e.$createElement,t=e._self._c||n;return t("h1",{staticClass:"my-3"},[t("span",{staticClass:"green--text font-weight-bold"},[e._v("You've completed section 2.")]),t("br"),e._v(" Here's a summary of your experiment participation. ")])}],i=t("5530"),o=t("2f62"),c={name:"ParticipationSummary",props:["experiment"],computed:Object(i["a"])(Object(i["a"])({},Object(o["c"])({consent:"consent/consent"})),{},{participationType:function(){var e="";switch(this.experiment.participationType){case"CONSENT":e="Invited students to consent";break;case"MANUAL":e="Manually determined students";break;case"AUTO":e="Automatically included all students";break}return e}}),methods:{nextSection:function(){this.$router.push({name:"AssignmentIntro",params:{experiment:this.experiment.experimentId}})}},beforeRouteEnter:function(e,n,t){e.meta.selectionType=n.meta.selectionType,t()}},p=c,r=(t("ec04"),t("2877")),l=t("6544"),u=t.n(l),m=t("8336"),x=t("cd55"),v=t("49e2"),d=t("c865"),f=t("0393"),h=Object(r["a"])(p,a,s,!1,null,null,null);n["default"]=h.exports;u()(h,{VBtn:m["a"],VExpansionPanel:x["a"],VExpansionPanelContent:v["a"],VExpansionPanelHeader:d["a"],VExpansionPanels:f["a"]})},ec04:function(e,n,t){"use strict";t("6fac")}}]);
//# sourceMappingURL=chunk-438edfb2.cadfa170.js.map