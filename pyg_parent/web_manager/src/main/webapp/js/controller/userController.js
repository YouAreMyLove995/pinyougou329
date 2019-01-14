 //控制层 
app.controller('userController' ,function($scope,$controller   ,newUserService){
	
	$controller('baseController',{$scope:$scope});//继承


    // 查询所有的用户的方法:
    $scope.findAll = function(){
        // 向后台发送请求:
        newUserService.findAll().success(function(response){
            $scope.list = response;
        });
    }
	
	//分页
	$scope.findPage=function(page,rows){
        newUserService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}


	
	//查询实体 
	$scope.findOne=function(id){
        newUserService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	

	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){
        newUserService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    // 显示状态
    $scope.status = ["已冻结","未冻结","关闭"];

    // 审核的方法:
    $scope.updateStatus = function(status){
        newUserService.updateStatus($scope.selectIds,status).success(function(response){
            if(response.flag){
                $scope.reloadList();//刷新列表
                $scope.selectIds = [];
            }else{
                alert(response.message);
            }
        });
    }
});	
