app.controller('saleMapController' ,function($scope ,$location,saleMapService){

    // AngularJS中的继承:伪继承
    $controller('baseController',{$scope:$scope});
	
	$scope.findSaleMap=function(){
        saleMapService.findSaleMap().success(function(response){
            $scope.list = response;
			}	
		);	
	}

});