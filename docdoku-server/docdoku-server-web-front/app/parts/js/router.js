/*global define,App*/
define([
    'backbone',
    'common-objects/common/singleton_decorator'
],
function (Backbone, singletonDecorator) {
    'use strict';
    var Router = Backbone.Router.extend({
        routes: {
            ':workspaceId/:partNumber/:partVersion':   'showPartRevision'
        },

        showPartRevision:function(workspace, partNumber, partVersion){
            App.appView.showPartRevision(workspace, partNumber, partVersion);
	    }
    });

    return singletonDecorator(Router);
});
