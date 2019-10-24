 //控制层 
app.controller('indexController' ,function($scope,$controller   ,contentService){
	
	$controller('baseController',{$scope:$scope});//继承


	$scope.contentList = [];
	$scope.findByCategoryId = function (catecoryId) {
		contentService.findByCategoryId(catecoryId).success(
			function (response) {
                $scope.contentList[catecoryId] =response;
            }
		)
    }
    
    $scope.search = function () {
		location.href = "http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }

    
});	