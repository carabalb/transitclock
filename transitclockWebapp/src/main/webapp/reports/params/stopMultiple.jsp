<%-- For creating a route selector parameter via a jsp include.
     User can select all routes (r param then set to " ") or any
     number of routes. 
     Reads in routes via API for the agency specified by the "a" param. --%>

<style type="text/css">
/* Set font for route selector. Need to use #select2-drop because of 
 * extra elements that select2 adds 
 */
#select2-drop, #routesDiv {
  font-family: sans-serif; font-size: large;
}
</style>
<script>
    $( document ).ready(function() {
        $("#allStops").click(function(){

            if($(this).prop("checked") == true){
                $("#stopIds").prop('disabled', true);
            }

            else if($(this).prop("checked") == false){
                $("#stopIds").prop('disabled', false);
            }

        });
    });

    $("form").submit(function(event){
        console.log("form submitted");
        if($("#allStops").prop("checked") == false){
            console.log("not checked, copy vals");
            $('#stopIdsHidden').val($('#stopIds').val());
            console.log("done");
        }
        else {
            $('#stopIdsHidden').val('');
        }
    });

</script>

<fieldset id="stopsFieldSet" >
    <legend >Stops</legend>
    <div id="stopsDiv"  class="param"  >
       <label for="allStops">All Stops:</label>
       <input id="allStops" type="checkbox" checked />
    </div>
    <div style="padding: 2px;">
      <label for="stopIds">Stops Ids:</label>
      <textarea style="width:500px;" id="stopIds" rows="7" disabled="true">306921,300505,981015,553137,403667,102411,403117,903050,503964,100646,551688,450338,903244,202097,405003,403560,803121,401732,553332,551609</textarea>
    </div>
    <input id="stopIdsHidden" type="hidden" name="s"/>
</fieldset>

    
