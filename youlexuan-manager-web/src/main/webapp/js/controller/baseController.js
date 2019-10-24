app.controller("baseController",function ($scope,$http) {

    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }

    }

    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }


    //定义对象保存打钩的id
    $scope.selectIds = [];
    $scope.updateSelection =function ($event,id) {
        if($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }

    }

    /**
     * 参数
     * 1、json串
     * 2、要取json串中的某个key对应的value
     * 思路，json串转成对象，遍历对象取key对应value进行拼接
     * [{"id":4,"text":"小米"},{"id":6,"text":"360"}]
     */

    $scope.jsonToStr=function (jsonStr,key) {
        var retValue = "";
        var json = JSON.parse(jsonStr);
        for(var i=0;i<json.length;i++){
            var obj = json[i];
            var value = obj[key];
            if(i>0){
                retValue = retValue +","
            }
            retValue = retValue + value;
        }
        return retValue;
    }

})