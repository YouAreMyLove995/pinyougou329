//服务层
app.service('sekillOrderService',function($http){

	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../sekillOrder/searchOrder.do?page='+page+"&rows="+rows, searchEntity);
	}    

});
