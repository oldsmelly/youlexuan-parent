 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search()['id'];
		if(id==null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//回显商品描述
                editor.html($scope.entity.goodsDesc.introduction);
                //回显图片
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //回显扩展属性
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //回显规格参数
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //回显sku中的spce
                for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec)
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){

		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add();//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	location.href='goods.html'//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}

	$scope.entity = {'goodsDesc':{'itemImages':[],'specificationItems':[]}};
	$scope.add = function(){
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add($scope.entity).success(
			function (response) {
				if(response.success){
					alert("添加成功");
					$scope.entity = {};
                    editor.html('');
				}else{
					alert(response.messages);
				}
            }
		)
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//上传的逻辑
	$scope.uploadFile =function () {
		uploadService.uploadFile().success(
			function (response) {
				if(response.success){
					$scope.image_entity.url = response.message;
				}else {
					alert(response.message);
				}
            }
		)
    }
    
    $scope.add_image_enttiy = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    $scope.removeImg = function ($index) {
        $scope.entity.goodsDesc.itemImages.splice($index,1);
    }


    //查询一级分类
    $scope.findItemCat1List = function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List =response;
            }
		)
    }

    //一级分类修改后联动出二级分类
    $scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat2List =response;
                }
            )
        }
    )

    //二级分类修改后联动出三级分类
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
            itemCatService.findByParentId(newValue).success(
                function (response) {
                    $scope.itemCat3List =response;
                }
            )
        }
    )

    //根据三级分类ID，查出分类的item
    $scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
            itemCatService.findOne(newValue).success(
                function (response) {
                    $scope.entity.goods.typeTemplateId =response.typeId;
                }
            )
        }
    )

    // 模板ID联动出 模板对象
    $scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
			typeTemplateService.findOne(newValue).success(
				function (response) {
					$scope.typeTemplate = response;
					//格式化json为对象
                    $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                    //[{"text":"内存大小","value":"10M"},{"text":"颜色","value":"红色"}]-->[{"text":"内存大小",value=111},{"text":"颜色"}]
					var id = $location.search()['id'];
					if(id==null){
                        $scope.entity.goodsDesc.customAttributeItems =  JSON.parse($scope.typeTemplate.customAttributeItems);
					}


                   //模板ID得到对应的规格和规格项
                   typeTemplateService.findSpecList(newValue).success(
						function (response) {
							$scope.specList = response;
						}
				   )
                }
			)
        }
    )

    /**
	 *  根据规格是否打钩加工对象
     	entity.goodsDesc.specificationItems=[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G","32G"]}]
     */

    $scope.updateSpecAttribute = function($event,specName,optionName){
    	//判断$scope.entity.goodsDesc.specificationItems对象中是否存在一个attributeName是specName的一个对象
    	var attrObject = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',specName);
    	if(attrObject==null){
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":specName,"attributeValue":[optionName]});
		}else{
    		if($event.target.checked){
                attrObject.attributeValue.push(optionName);
			}else{
    			var index =attrObject.attributeValue.indexOf(optionName);
                attrObject.attributeValue.splice(index,1);
                if(attrObject.attributeValue.length==0){
                	var objIndex = $scope.entity.goodsDesc.specificationItems.indexOf(attrObject);
                    $scope.entity.goodsDesc.specificationItems.splice(objIndex,1);
				}
			}
		}


	}

    /**
	 * 根据选择的规格动态生成sku列表
	 * $scope.entity.goodsDesc.specificationItems = [{"attributeName":"网络","attributeValue":["移动4G","联通3G"]},{"attributeName":"机身内存","attributeValue":["32G","64G","128G"]}]
	 * $scope.entity.itemList = [{spce:{{"机身内存":"32G","网络":"移动4G"},price:0,num:999,status:'0',idDefault:'0'},{spce:{{"机身内存":"62G","网络":"移动4G"},price:0,num:999,status:'0',idDefault:'0'}}}]
     */
    $scope.creatItemList = function () {
    	$scope.entity.itemList = [{spec:{},price:0,num:99999,status:'0',idDefault:'0'}]; //初始化一个skuList
		var items = $scope.entity.goodsDesc.specificationItems;
		for(var i=0;i<items.length;i++){
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }

    /**
	 *
     * @param list          $scope.entity.itemList
     * @param columnName    网络
     * @param columnValues  ["移动4G","联通3G"]
     */
    addColumn = function (list,columnName,columnValues) {
		var newList = []; //返回加工后的新list
		for (var i=0;i<list.length;i++){
			var oldRow = list[i]; //{spce:{{"机身内存":"32G","网络":"移动4G"},price:0,num:999,status:'0',idDefault:'0'}
			for(var j=0;j<columnValues.length;j++){
                var  newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
				newRow.spec[columnName]=columnValues[j];
                newList.push(newRow);
			}
		}
		return newList;
    }

    $scope.status = ['待审核','审核通过','审核驳回','删除']; //500万
	$scope.itemCatList=[];
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {
				for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id] = response[i].name;
				}

            }
		)
    }
    
    $scope.checkAttributeValue = function (specName,optionName) {
		var items = $scope.entity.goodsDesc.specificationItems
		var obj = $scope.searchObjectByKey(items,'attributeName',specName);
		if(obj==null){
			return false;
		}else {
           if(obj.attributeValue.indexOf(optionName)>=0) {
           		return true;
		   }else {
           		return false;
		   }
		}
    }


    
});	