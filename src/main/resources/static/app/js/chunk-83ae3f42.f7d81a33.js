(window["webpackJsonp"]=window["webpackJsonp"]||[]).push([["chunk-83ae3f42"],{3860:function(e,t,o){"use strict";var s=o("604c");t["a"]=s["a"].extend({name:"button-group",provide:function(){return{btnToggle:this}},computed:{classes:function(){return s["a"].options.computed.classes.call(this)}},methods:{genData:s["a"].options.methods.genData}})},5100:function(e,t,o){"use strict";o.r(t);var s=function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",[o("h1",[e._v(" Because you have "),o("strong",[e._v(e._s(e.numberOfConditions)+" conditions")]),e._v(" and would like your students to be "),"WITHIN"===e.exposureType?[o("strong",[e._v("exposed to every conditions")]),e._v(" (within-subject), ")]:[o("strong",[e._v("exposed to only one condition")]),e._v(" (between), ")],e._v(" we will set you up with "+e._s(e.numberOfExperimentSets)+" exposure sets. ")],2),o("div",{staticClass:"mt-3"},[o("strong",[e._v(" Exposure Set: ")]),o("v-btn-toggle",{staticClass:"ml-3",attrs:{color:"primary",mandatory:""},model:{value:e.selectedExposure,callback:function(t){e.selectedExposure=t},expression:"selectedExposure"}},e._l(e.exposures,(function(t,s){return o("v-btn",{key:t.exposureId,attrs:{value:t}},[e._v(e._s(s+1))])})),1)],1),o("v-card",{staticClass:"mt-5 pa-5 mx-auto lighten-5 rounded-lg",attrs:{outlined:""}},e._l(e.sortedGroups(),(function(t){return o("p",{key:t,staticClass:"pa-0 my-0"},[e._v(" "+e._s(t)+" will receive "),o("v-chip",{staticClass:"ma-2",attrs:{color:"primary",label:""}},[e._v(" "+e._s(e.groupNameConditionMapping[t]))])],1)})),0),o("v-btn",{staticClass:"mt-5",attrs:{elevation:"0",color:"primary",to:{name:"AssignmentExposureSetsIntro",params:{numberOfExperimentSets:this.numberOfExperimentSets,exposure_id:this.selectedExposure.exposureId}}}},[e._v("Continue ")])],1)},n=[],r=o("5530"),i=(o("d81d"),o("2f62")),u=o("4360"),a={name:"AssignmentExposureSets",props:["experiment"],data:function(){return{selectedExposure:[]}},computed:Object(r["a"])(Object(r["a"])({},Object(i["c"])({exposures:"exposures/exposures"})),{},{exposureType:function(){return this.experiment.exposureType},numberOfConditions:function(){return this.experiment.conditions.length},numberOfExperimentSets:function(){return this.exposures.length},groupNameConditionMapping:function(){var e,t={};return null===(e=this.selectedExposure.groupConditionList)||void 0===e||e.map((function(e){return t[e.groupName]=e.conditionName})),t}}),methods:Object(r["a"])(Object(r["a"])({},Object(i["b"])({fetchExposures:"exposures/fetchExposures"})),{},{sortedGroups:function(){var e,t,o=null===(e=this.selectedExposure)||void 0===e||null===(t=e.groupConditionList)||void 0===t?void 0:t.map((function(e){return e.groupName}));return null===o||void 0===o?void 0:o.sort()},saveExit:function(){this.$router.push({name:"Home"})}}),beforeRouteEnter:function(e,t,o){return u["a"].dispatch("exposures/fetchExposures",e.params.experiment_id).then(o,o)},beforeRouteUpdate:function(e,t,o){return u["a"].dispatch("exposures/fetchExposures",e.params.experiment_id).then(o,o)}},p=a,l=o("2877"),c=o("6544"),d=o.n(c),m=o("8336"),h=(o("7e58"),o("3860")),g=o("a9ad"),v=o("58df"),x=Object(v["a"])(h["a"],g["a"]).extend({name:"v-btn-toggle",props:{backgroundColor:String,borderless:Boolean,dense:Boolean,group:Boolean,rounded:Boolean,shaped:Boolean,tile:Boolean},computed:{classes:function(){return Object(r["a"])(Object(r["a"])({},h["a"].options.computed.classes.call(this)),{},{"v-btn-toggle":!0,"v-btn-toggle--borderless":this.borderless,"v-btn-toggle--dense":this.dense,"v-btn-toggle--group":this.group,"v-btn-toggle--rounded":this.rounded,"v-btn-toggle--shaped":this.shaped,"v-btn-toggle--tile":this.tile},this.themeClasses)}},methods:{genData:function(){var e=this.setTextColor(this.color,Object(r["a"])({},h["a"].options.methods.genData.call(this)));return this.group?e:this.setBackgroundColor(this.backgroundColor,e)}}}),b=o("b0af"),f=o("cc20"),_=Object(l["a"])(p,s,n,!1,null,null,null);t["default"]=_.exports;d()(_,{VBtn:m["a"],VBtnToggle:x,VCard:b["a"],VChip:f["a"]})},"7e58":function(e,t,o){}}]);
//# sourceMappingURL=chunk-83ae3f42.f7d81a33.js.map