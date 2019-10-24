app.controller("brandController",function ($scope,$controller,brandService) {
    //继承父controller
    $controller('baseController',{$scope:$scope});


    $scope.findPage = function (pageNum,pageSize) {
        brandService.findPage().success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }

    /**
     * @param pageNum
     * @param pageSize
     * 根据条件模糊查询
     */
    $scope.searchEntity={};//定义搜索对象
    $scope.search = function (pageNum,pageSize) {
        brandService.search(pageNum,pageSize,$scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;
            }
        )
    }

    $scope.save = function () {
        brandService.save($scope.entity).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.messages);
                }
            }
        )
    }

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }

    $scope.delet = function () {
        brandService.delet($scope.selectIds).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.messages);
                }
            }
        )
    }

})