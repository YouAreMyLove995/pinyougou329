 //控制层 
app.controller('sekillOrderController' ,function($scope,$controller, sekillOrderService){
	
	$controller('baseController',{$scope:$scope});//继承


	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){
        sekillOrderService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
	// 显示状态
	$scope.status = ["未审核","审核通过","审核未通过","关闭"];

	// 审核的方法:
	$scope.updateStatus = function(status){
        sekillOrderService.updateStatus($scope.selectIds,status).success(function(response){
			if(response.flag){
				$scope.reloadList();//刷新列表
				$scope.selectIds = [];
			}else{
				alert(response.message);
			}
		});
	}

});	
