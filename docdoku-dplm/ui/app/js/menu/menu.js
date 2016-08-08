angular.module('dplm.menu', [])
    .directive('menuButton',function(){
        return {
            restrict:'E',
            templateUrl:'js/menu/menu-button.html',
            scope:false
        };
    })
    .controller('MenuController', function ($scope,FolderService) {
        $scope.onFileDropped = function(path){
            if(path){
                FolderService.add(path);
            }
        };

    })

    .controller('FolderMenuController', function ($scope) {

        $scope.onDrop = function () {
        };

    })
    .controller('WorkspaceMenuController', function ($scope, WorkspaceService) {

        $scope.onDrop = function () {
        };

        $scope.refreshWorkspaces = function(){
            WorkspaceService.reset();
            WorkspaceService.getWorkspaces();
        };

    });