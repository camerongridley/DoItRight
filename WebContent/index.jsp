<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>


<c:import url="header.jsp" />
		
<div class="container">
	<div class="row">
		<div class="col-md-12">
			<form class="form-inline" action="./LoadData" method="POST">
				<div style="margin-bottom:20px;">To get started testing, load the test data by clicking below.</div>
				<div><button type="submit" class="btn btn-primary">Load Data</button></div>
			</form>
		</div>		
	</div>	
</div>


<c:import url="footer.jsp" />