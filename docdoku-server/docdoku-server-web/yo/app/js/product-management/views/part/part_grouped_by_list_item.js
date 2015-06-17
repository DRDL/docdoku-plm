/*global _,define,App*/
define([
    'backbone',
    'mustache',
    'text!templates/part/part_grouped_by_list_item.html',
    'common-objects/utils/date',
    'common-objects/models/part',
    'common-objects/views/part/part_modal_view'
], function (Backbone, Mustache, template, Date, Part, PartModalView){
    'use strict';
    var PartGroupedByListItemView = Backbone.View.extend({

        events: {
        },

        tagName:'tr',

        initialize: function () {
            this.item = this.options.item;
            this.headerColumns = this.options.columns;
            this.queryFilters = this.options.queryFilters;
        },

        render: function () {
            var self = this;
            var itemOrdered = [];

            _.each(this.headerColumns, function(column){

                var value = self.item[column.value];

                var filter = _.findWhere(self.queryFilters, {id : column.value});
                var type = filter ? filter.realType : 'string';

                var isDate = type === 'date';
                var isPartNumber = type ==='partNumber';
                var isLinkedDocuments = type ==='linkedDocuments';

                var isStringValue = !isDate && !isPartNumber && !isLinkedDocuments;

                if(isDate) {
                    if (!value) {
                        value = '';
                    }
                    var timestampFormated = Date.formatTimestamp(
                        App.config.i18n._DATE_FORMAT,
                        value
                    );

                    value = timestampFormated ? timestampFormated : '';
                }

                if(isLinkedDocuments){
                    if (!value) {
                        value = [];
                    }
                    else{
                        var documents = value.split(',');
                        documents.pop();
                        var result = [];
                        _.each(documents,function(document){
                            var lastDash = document.lastIndexOf('-');
                            var id = document.substr(0, lastDash);
                            var version = document.substr(lastDash+1);
                            result.push({
                                link:'../documents/'+App.config.workspaceId+'/'+id+'/'+version,
                                name:document
                            });
                        });
                        value = result;
                    }
                }

                var itemColumn = {
                    isDate : isDate,
                    isPartNumber : isPartNumber,
                    isStringValue : isStringValue,
                    isLinkedDocuments : isLinkedDocuments,
                    value : value
                };
                itemOrdered.push(itemColumn);
            });

            this.$el.html(Mustache.render(template, {
                i18n: App.config.i18n,
                columns: itemOrdered
            }));

            this.$el.on('click', this.openModal.bind(this));

            Date.dateHelper(this.$('.date-popover'));
            return this;
        },

        openModal:function(e){

            if(e.target.className === 'linkedDocument'){
                return;
            }

            var model = new Part({partKey:this.item['pr.partKey']});
            model.fetch().success(function () {
                var partModalView = new PartModalView({
                    model: model
                });
                partModalView.show();
            });
        }

    });

    return PartGroupedByListItemView;
});
