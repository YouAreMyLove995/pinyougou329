//服务层
app.service('sekillService',function($http){

	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../sekill/search.do?page='+page+"&rows="+rows, searchEntity);
	}    
	
	this.updateStatus = function(ids,status){
		return $http.get('../sekill/updateStatus.do?ids='+ids+"&status="+status);
	}

});
