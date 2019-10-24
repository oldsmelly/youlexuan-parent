 //控制层 
app.controller('cartController' ,function($scope,$controller   ,cartService){
	
	$controller('baseController',{$scope:$scope});//继承


    $scope.findCartList = function () {
        cartService.findCartList().success(
        	function (response) {
				$scope.cartList = response;
				$scope.totalValue = cartService.sum($scope.cartList)



            }
		)
    }
    
    $scope.addGoodsToCart = function (itemId,num) {
		cartService.addGoodsToCart(itemId,num).success(
			function (respons) {
				if(respons.success){
					$scope.findCartList();//刷新列表页
				}else{
					alert(respons.message);
				}
            }
		)
    }
    
    $scope.findAddressList = function () {
		cartService.findAddressList().success(
			function (response) {
				$scope.addressList = response;
				for (var i=0;i<$scope.addressList.length;i++){
					if($scope.addressList[i].isDefault=='1'){
                        $scope.address =$scope.addressList[i];
                        break;
                    }
				}
            }
		)
    }
    
    $scope.selectAddress =function(addr) {
		$scope.address = addr;
    }

    $scope.isSelectedAddress = function (addr) {
		if(addr==$scope.address){
			return true;
		}else {
			return false;
		}
    }
    /**
	 * 加工提交订单的 订单信息
     * @param type
     */
    $scope.order={'paymentType':'1'}
    $scope.selectPayType = function (type) {
		$scope.order.paymentType=type;
    }

    /**
	 * 提交订单信息
     */
    $scope.submitOrder = function () {
		//加工数据
		$scope.order.receiverAreaName = $scope.address.address;
		$scope.order.receiverMobile = $scope.address.mobile;
		$scope.order.receiver = $scope.address.contact;

		cartService.submitOrder($scope.order).success(
			function (resopons) {
				if(resopons.success){
					if($scope.order.paymentType=='1'){
						window.location.href="pay.html";
					}else {
						alert("订单提交成功");
					}
				}else{
					alert(resopons.message)
				}
            }
		)



    }

    
});	