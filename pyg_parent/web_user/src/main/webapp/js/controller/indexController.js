//首页控制器
app.controller('indexController',function($scope,loginService,userService){

    // AngularJS中的继承:伪继承
    $controller('baseController',{$scope:$scope});

	$scope.showName=function(){
			loginService.showName().success(
					function(response){
						$scope.loginName=response.loginName;
					}
			);
	}

    // 查询所有的品牌列表的方法:
    $scope.findAll = function(){
        // 向后台发送请求:
        userService.findAll().success(function(response){
            $scope.list = response;
        });
    }

    // 分页查询
    $scope.findPage = function(page,rows){
        // 向后台发送请求获取数据:
        userService.findPage(page,rows).success(function(response){
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        });
    }

    // 分页查询
    $scope.search = function(page,rows){
        // 向后台发送请求获取数据:
        userService.search(page,rows).success(function(response){
            $scope.paginationConf.totalItems = response.total;
            $scope.list = response.rows;
        });
    }


    $scope.searchEntity={};


    // 显示状态
    $scope.status = ["待付款","已付款","待发货","已发货"];

    // 审核的方法:
    $scope.updateStatus = function(status){
        userService.updateStatus($scope.selectIds,status).success(function(response){
            if(response.flag){
                $scope.reloadList();//刷新列表
                $scope.selectIds = [];
            }else{
                alert(response.message);
            }
        });
    }

});