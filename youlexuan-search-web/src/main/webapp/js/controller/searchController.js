 //控制层 
app.controller('searchController' ,function($scope,$controller,$location,searchService){
	
	$controller('baseController',{$scope:$scope});//继承


	$scope.search = function () {
		searchService.search($scope.searchMap).success(
			function (response) { //map
				$scope.resultMap = response;
                buildPageLabel();
            }
		)
    }

    /**
	 * 构造搜索的过滤条件
     */
    $scope.searchMap = {'keywords':'','category':'','brand':'','price':'','spec':{}} //搜索条件的对象
	$scope.addSearchParam = function (key,value) {
		if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
		}

		$scope.search();
    }

    $scope.removeSearchParam = function(key){
        if(key=='category'||key=='brand'||key=='price'){
            $scope.searchMap[key]='';
        }else{
           delete $scope.searchMap.spec[key];
        }
        $scope.search();
	}

    /**
     * 构造分页标签
     * 如果最大页不足5页，那么显示1-mapPage
     * 如果最大页多余5页，中间显示当前页，前后各两个数
     * 		如果当前页前面没有2页，前面补充
     * 		如果当前页后面没有2业，后面补充
     *
     */

    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//开始页码
        var lastPage=$scope.resultMap.totalPage;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if($scope.resultMap.totalPage>5){  //如果页码数量大于5
            if($scope.searchMap.pageNo<=3){//如果当前页码小于等于3 ，显示前5页
                lastPage=5;
                $scope.firstDot=false;//前面没点
            }else if( $scope.searchMap.pageNo>= $scope.resultMap.totalPages-2 ){//显示后5页
                firstPage=$scope.resultMap.totalPages-4;
                $scope.lastDot=false;//后边没点
            }else{  //显示以当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }
        //构建页码
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
//判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }


    $scope.queryByPage = function (pageNo) {
        // alert(pageNo);
        //页码验证
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }

        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    /**
     * 排序
     */
    $scope.sortSearch = function (sortField,sortValue) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sortValue = sortValue;
        $scope.search();
    }

    /**
     * 判断关键字是不是品牌
     */
    $scope.keywordsIsBrand = function() {
        //alert();
        for(var i=0;i<$scope.resultMap.brandList.length;i++){
            var brand = $scope.resultMap.brandList[i].text;
           // alert(brand);
           // alert("kw"+$scope.searchMap.keywords);
            if($scope.searchMap.keywords.indexOf(brand)>=0){
                return true;
            }
        }
        return false
    }

    /**
     * 页面初始化后加载portal工程传来的keyword
     */
    $scope.loadKeywords = function () {
       $scope.searchMap.keywords = $location.search()['keywords'];
       $scope.search();
    }


});	