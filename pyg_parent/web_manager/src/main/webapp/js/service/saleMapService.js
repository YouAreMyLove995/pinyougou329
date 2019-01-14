app.service('saleMapService',function($http){

    this.findSaleMap = function(){
        return $http.get("../sale/findSaleMap.do");
    }
	

});