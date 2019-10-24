//服务层
app.service('payService',function($http){
	    	

	this.creatNative = function () {
        return $http.get('../pay/creatNative.do');
    }
    
    this.queryPayStatus = function (out_trade_no) {
        return $http.get("../pay/queryPayStatus.do?out_trade_no="+out_trade_no);
    }


});