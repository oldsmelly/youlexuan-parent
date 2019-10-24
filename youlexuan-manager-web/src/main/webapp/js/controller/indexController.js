app.controller("indexController",function ($scope,$controller,loginService) {

    $controller('baseController',{$scope:$scope});
    $scope.getLoginName = function () {
        loginService.getLoginName().success(
            function (response) {
                $scope.userName = response.userName;
                $scope.lastLoginTime = response.lastLoginTime;
            }
        )
    }
})