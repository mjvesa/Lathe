  
  let counter = 0;
  let PointBeingMoved = -1;
  let PointCount=0;
  let light = [];
  let texture = [];
  let renderMethod = "point";
  let lathePoints = [];
  
  calculateLight();
  calculateTexture();
  
  document.querySelector('#render-select').oninput = event => {
    renderMethod = event.target.value
  }
  
  function calculateLight() {
    for (let i=0;i<65536;i++) {
      light[i]=0;
    }
    
    for (let j=0;j<10;j++) {
      let k=5487*j+736;
      let i=k*k*k; //i is now random enough..   
      
      for (let y=-128;y<128;y++) {
        for (let x=-128;x<128;x++) {
          let color =10000 / (((x*x+y*y) / 6)+1);
          color+=light[i&65535];
          if (color>255) color=255;
          light[i&65535]= color;
          i++;
        }
      }
    }
  }

  function calculateTexture() {
    for (let i=0;i<65536;i++) {
      texture[i]=0;
    }
    for (let j=0;j<100;j++) {
      let k=5483*j+736;
      let i=k*k*k; //i is now random enough..   
      for (let y=-128;y<128;y++) {
        for (let x=-128;x<128;x++) {
          texture[i&65535]=(texture[i&65535]+((x*x+y*y) / 128))&255;
          i++;
        }
      }
    }
    
    for (let i=0;i<65536;i++) {
      texture[i]=((texture[i]-128)*(texture[i]-128)) / 64;
    }
  }
  
  
  function addPoint(x,y) {
    lathePoints[PointCount*2]=x;
    lathePoints[PointCount*2+1]=y;
    PointCount++;
    drawPoints();
    drawObject(0,0);
  }

  function movePoint(index,x,y) {
    
    lathePoints[index*2]=x;
    lathePoints[index*2+1]=y;
    drawPoints();
    drawObject(0,0);
  }

  
   function getPointIfHit(x,y) {
    let PointFound=false;
    
    for (let i=0;i<PointCount;i++) {
      
      let px = lathePoints[i*2];
      let py = lathePoints[i*2+1];
      
      if (((px-x)*(px-x)+(py-y)*(py-y))<50) {
        return i;
      }
    }
    
    return -1;
    
  }
  
   function deletePoint(x,y) {
    
    let PointFound=false;
    
    for (let i=0;i<PointCount;i++) {
      
      let px = lathePoints[i*2];
      let py = lathePoints[i*2+1];
      
      if (((px-x)*(px-x)+(py-y)*(py-y))<50) {
        for (let j=i;j<PointCount;j++) {
          lathePoints[j*2]=Points[(j+1)*2];
          lathePoints[j*2+1]=Points[(j+1)*2+1];
        }
        PointCount--;
        PointFound=true;
        break;
      }
    }
    
    if (PointFound) {
      drawPoints();
      drawObject(0,0);
    }
  }
  

  
   function drawPoints() {
  
      let canvas = document.getElementById('editorcanvas');
      let ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, canvas.width, canvas.height);

      
      
      ctx.beginPath();
      ctx.moveTo(lathePoints[0], lathePoints[1]);
      for (let i=0;i<PointCount;i++) {
      
        let x = lathePoints[i*2];
        let y = lathePoints[i*2+1];
        
        ctx.lineTo(x,y);
        ctx.strokeRect(x-5,y-5,10,10);
      }
      ctx.stroke();
  };

  

    function drawObject( x, y) {


    if (PointCount < 2) {
      return;
    }

    let zBuffer= [];
    
    for (let i =0;i<400*400;i++) {
      zBuffer[i] = 100000;
    }


    leterp = function(a,b,c) {
              return ((b-a)*c+a*10)/10;
    }

    drawPoly = function(a,b,c,color,canvasData) {
      let tmp;
      let index = 0;
      let x,y;
        
      for (let i=0;i<10;i++) {
          for (let j=0;j<10;j++) {
               x=leterp(leterp(a.x,c.x,i),leterp(a.x,b.x,i),j);
               y=leterp(leterp(a.y,c.y,i),leterp(a.y,b.y,i),j);
               
               index = (Math.floor(x)+Math.floor(y)*400)*4;
               canvasData.data[index]=color;
               canvasData.data[index+1]=color;
               canvasData.data[index+2]=color;
               canvasData.data[index+3]=255;
        }
      }
    }
    
    //Filled polygon////////////////////////////////////////////////////////
    
    filledHLine = function(x1,x2,z,y,color,canvasData) {
    
      if (x2<x1) {
        let temp = x1;
        x1=x2;
        x2=temp;
      }
      
      let i = Math.floor(x1)+Math.floor(y)*400;
      let ptr = i*4;
      for (let x = x1;x<x2; x++) {
      
        if (zBuffer[i]>z) {
          zBuffer[i]=z;
                canvasData.data[ptr]=color;
                canvasData.data[ptr+1]=color;
                canvasData.data[ptr+2]=color;
                canvasData.data[ptr+3]=255;
              }
              ptr=ptr+4;
              i=i+1;
          }
    }
          
    drawFilledPoly = function(a,b,c,color,canvasData) {
    
      let temp;
      
      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }
      
      if (b.y>c.y) {
        temp=b;
        b=c;
        c=temp;
      }

      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }
      
      let leftXDelta = (b.x-a.x) / (b.y-a.y+1);
      let rightXDelta = (c.x-a.x) / (c.y-a.y+1);
      
      leftX=a.x;
      rightX=a.x;

      let z = (a.z+b.z+c.z) / 3;
    
      for (let sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
        filledHLine(Math.floor(leftX),Math.floor(rightX),z,sy,color,canvasData);
        leftX=leftX+leftXDelta;
        rightX=rightX+rightXDelta;
      }
      
      leftXDelta = (c.x-b.x) / (c.y-b.y+1);
      leftX=b.x;

      for (let sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
        filledHLine(Math.floor(leftX),Math.floor(rightX),z,sy,color,canvasData);
        leftX=leftX+leftXDelta;
        rightX=rightX+rightXDelta;
      }
    }
    
    //Textured polygon//////////////////////////////////////////////////////

    
      texturedHLine = function(left,right,z,y,canvasData) {
    
      if (right.x<left.x) {
        let temp = right;
        right=left;
        left=temp;
      }

      let leftX = Math.floor(left.x);
      let rightX = Math.floor(right.x);

      let tx=left.tx;
      let ty=left.ty;

      let txDelta = (right.tx-left.tx) / (rightX-leftX+1);
      let tyDelta = (right.ty-left.ty) / (rightX-leftX+1);
      
      let i = Math.floor(leftX)+Math.floor(y)*400;
      let ptr = i*4;
      for (let x = leftX;x<rightX; x++) {
      
        if (zBuffer[i]>z) {
          zBuffer[i]=z;
          color = texture[(Math.floor(tx)&255)+(Math.floor(ty)&255)*256];
                canvasData.data[ptr]=color;
                canvasData.data[ptr+1]=color;
                canvasData.data[ptr+2]=color;
                canvasData.data[ptr+3]=255;
              }
        tx = tx + txDelta;
        ty = ty + tyDelta;
              ptr=ptr+4;
              i=i+1;
          }
    }

    drawTexturedPoly = function(a,b,c,canvasData) {

      let temp;
      
      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }
      
      if (b.y>c.y) {
        temp=b;
        b=c;
        c=temp;
      }

      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }

      let left = new Object();
      let right = new Object();
      
      left.xDelta = (b.x-a.x) / (b.y-a.y+1);
      right.xDelta = (c.x-a.x) / (c.y-a.y+1);
      left.txDelta = (b.tx-a.tx) / (b.y-a.y+1);
      right.txDelta = (c.tx-a.tx) / (c.y-a.y+1);
      left.tyDelta = (b.ty-a.ty) / (b.y-a.y+1);
      right.tyDelta = (c.ty-a.ty) / (c.y-a.y+1);
      
      left.x=a.x;
      right.x=a.x;
      left.tx=a.tx;
      right.tx=a.tx;
      left.ty=a.ty;   
      right.ty=a.ty;

      let z = (a.z+b.z+c.z) / 3;
    
      for (let sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
        texturedHLine(left,right,z,sy,canvasData);
        
        left.x=left.x+left.xDelta;
        right.x=right.x+right.xDelta;
        left.tx=left.tx+left.txDelta;
        right.tx=right.tx+right.txDelta;
        left.ty=left.ty+left.tyDelta;
        right.ty=right.ty+right.tyDelta;
      }
      
      left.xDelta = (c.x-b.x) / (c.y-b.y+1);
      left.txDelta = (c.tx-b.tx) / (c.y-b.y+1);
      left.tyDelta = (c.ty-b.ty) / (c.y-b.y+1);
      left.x=b.x;
      left.tx=b.tx;
      left.ty=b.ty;

      for (let sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
        texturedHLine(left,right,z,sy,canvasData);
        
        left.x=left.x+left.xDelta;
        right.x=right.x+right.xDelta;
        left.tx=left.tx+left.txDelta;
        right.tx=right.tx+right.txDelta;
        left.ty=left.ty+left.tyDelta;
        right.ty=right.ty+right.tyDelta;
      }
    }

    //Envmapped and textured polygon///////////////////////////////////////
    
      shadedTexturedHLine = function(left,right,z,y,canvasData) {
    
      if (right.x<left.x) {
        let temp = right;
        right=left;
        left=temp;
      }

      let leftX = Math.floor(left.x);
      let rightX = Math.floor(right.x);

      let tx=left.tx;
      let ty=left.ty;
      let nx=left.nx;
      let ny=left.ny;

      let txDelta = (right.tx-left.tx) / (rightX-leftX+1);
      let tyDelta = (right.ty-left.ty) / (rightX-leftX+1);
      let nxDelta = (right.nx-left.nx) / (rightX-leftX+1);
      let nyDelta = (right.ny-left.ny) / (rightX-leftX+1);
      
      let i = Math.floor(leftX)+Math.floor(y)*400;
      let ptr = i*4;
      for (let x = leftX;x<rightX; x++) {
      
        if (zBuffer[i]>z) {
          zBuffer[i]=z;
          color = texture[(Math.floor(tx)&255)+(Math.floor(ty)&255)*256] / 2;
          shade = light[(Math.floor(nx)&255)+(Math.floor(ny)&255)*256];
                canvasData.data[ptr]=color+shade;
                canvasData.data[ptr+1]=shade;
                canvasData.data[ptr+2]=color+shade;
                canvasData.data[ptr+3]=255;
              }
        tx = tx + txDelta;
        ty = ty + tyDelta;
        nx = nx + nxDelta;
        ny = ny + nyDelta;
              ptr=ptr+4;
              i=i+1;
          }
    }

    drawShadedTexturedPoly = function(a,b,c,canvasData) {

      let temp;
      
      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }
      
      if (b.y>c.y) {
        temp=b;
        b=c;
        c=temp;
      }

      if (a.y>b.y) {
        temp=a;
        a=b;
        b=temp;
      }

      let left = new Object();
      let right = new Object();
      
      left.xDelta = (b.x-a.x) / (b.y-a.y+1);
      right.xDelta = (c.x-a.x) / (c.y-a.y+1);
      left.txDelta = (b.tx-a.tx) / (b.y-a.y+1);
      right.txDelta = (c.tx-a.tx) / (c.y-a.y+1);
      left.tyDelta = (b.ty-a.ty) / (b.y-a.y+1);
      right.tyDelta = (c.ty-a.ty) / (c.y-a.y+1);
      left.nxDelta = (b.nx-a.nx) / (b.y-a.y+1);
      right.nxDelta = (c.nx-a.nx) / (c.y-a.y+1);
      left.nyDelta = (b.ny-a.ny) / (b.y-a.y+1);
      right.nyDelta = (c.ny-a.ny) / (c.y-a.y+1);
      
      left.x=a.x;
      right.x=a.x;
      left.tx=a.tx;
      right.tx=a.tx;
      left.ty=a.ty;   
      right.ty=a.ty;
      left.nx=a.nx;
      right.nx=a.nx;
      left.ny=a.ny;   
      right.ny=a.ny;

      let z = (a.z+b.z+c.z) / 3;
    
      for (let sy=Math.floor(a.y);sy<Math.floor(b.y);sy++) {
        shadedTexturedHLine(left,right,z,sy,canvasData);
        
        left.x=left.x+left.xDelta;
        right.x=right.x+right.xDelta;
        left.tx=left.tx+left.txDelta;
        right.tx=right.tx+right.txDelta;
        left.ty=left.ty+left.tyDelta;
        right.ty=right.ty+right.tyDelta;
        left.nx=left.nx+left.nxDelta;
        right.nx=right.nx+right.nxDelta;
        left.ny=left.ny+left.nyDelta;
        right.ny=right.ny+right.nyDelta;
      }
      
      left.xDelta = (c.x-b.x) / (c.y-b.y+1);
      left.txDelta = (c.tx-b.tx) / (c.y-b.y+1);
      left.tyDelta = (c.ty-b.ty) / (c.y-b.y+1);
      left.nxDelta = (c.nx-b.nx) / (c.y-b.y+1);
      left.nyDelta = (c.ny-b.ny) / (c.y-b.y+1);
      left.x=b.x;
      left.tx=b.tx;
      left.ty=b.ty;
      left.nx=b.nx;
      left.ny=b.ny;

      for (let sy=Math.floor(b.y);sy<Math.floor(c.y);sy++) {
        shadedTexturedHLine(left,right,z,sy,canvasData);
        
        left.x=left.x+left.xDelta;
        right.x=right.x+right.xDelta;
        left.tx=left.tx+left.txDelta;
        right.tx=right.tx+right.txDelta;
        left.ty=left.ty+left.tyDelta;
        right.ty=right.ty+right.tyDelta;
        left.nx=left.nx+left.nxDelta;
        right.nx=right.nx+right.nxDelta;
        left.ny=left.ny+left.nyDelta;
        right.ny=right.ny+right.nyDelta;
      }
    }
    
    //Rotation and rendering////////////////////////////////////////////////

      
      let canvas = document.getElementById('rendercanvas');
      let ctx = canvas.getContext('2d');
      let canvasData = ctx.createImageData(canvas.width, canvas.height);
      
      let lineNorms = [];

      for (let i=0;i<PointCount-1; i++) {
      
        normal = new Object();
      
      let nx = -(lathePoints[(i+1)*2+1]-lathePoints[i*2+1]);
      let ny = lathePoints[(i+1)*2]-lathePoints[i*2];
      let length = Math.sqrt(nx*nx+ny*ny);
      
        normal.x=nx / length;
        normal.y=ny / length;

      lineNorms[i]=normal;
      }
      
      let PointNorms = [];
    
      for (let i=0;i<PointCount; i++) {

      let normal = new Object();

      if (i===0) {
        normal.x=lineNorms[0].x;
        normal.y=lineNorms[0].y;
      } else if (i===PointCount-1) {
        normal.x=lineNorms[PointCount-2].x;
        normal.y=lineNorms[PointCount-2].y;
      } else {
        normal.x=(lineNorms[i-1].x+lineNorms[i].x)/2;
        normal.y=(lineNorms[i-1].y+lineNorms[i].y)/2;
      }

      PointNorms[i]=normal;
    }
    
      let Points = [];
      let count = 0;
      let PointIndex = 0;

    for (let i=0;i<PointCount;i++) {
      for (let j=0;j<10;j++) {
      
        let pi = 3.14159265358979323846264338327950288419716939937510;

        //let distort =20; //+ ((Math.sin(j*pi*2/2.5)+Math.sin(i*pi*2/5))*5);
        

        let r = lathePoints[PointIndex];
        let y1 = lathePoints[PointIndex+1]-200;
        let x1 = r*Math.cos(j*2*pi/9);
        let z1 = r*Math.sin(j*2*pi/9);

        let ny1 = PointNorms[i].y;
        let nx1 = PointNorms[i].x*Math.cos(j*2*pi/9);
        let nz1 = PointNorms[i].x*Math.sin(j*2*pi/9);

        let cx = Math.cos(x*pi/200);
        let sx = Math.sin(x*pi/200);
  
        let cy = Math.cos(y*pi/200);
        let sy = Math.sin(y*pi/200);
        
        let y2 = cx * y1+ sx * z1;
        let z2 = sx * y1- cx * z1;  

        let x2 = cy * x1+ sy * z2;
        let z3 = sy * x1- cy * z2;

        let ny2 = cx * ny1+ sx * nz1;
        let nz2 = sx * ny1- cx * nz1;  

        let nx2 = cy * nx1+ sy * nz2;
        let nz3 = sy * nx1- cy * nz2;
        
        Points[count]= new Object();
        
        Points[count].x=200+(x2*256 / (z3+500));
        Points[count].y=200+(y2*256 / (z3+500));
        Points[count].z=128+z3;

        Points[count].tx=j*28.45;
        Points[count].ty=i*28.45;

        Points[count].nx=nx2*128+128;
        Points[count].ny=ny2*128+128;
        Points[count].nz=nz3*128+128;
        count=count+1;
      }
        PointIndex+=2;
    }

    count=0;
    
        console.log('blvaslbls' + renderMethod);

        
    for (let i=0;i<PointCount-1;i++) {
      for (let j=0;j<9;j++) {
      
        let a = j+count;
        let b = (j+1)+count;
        let c = j+count+10;
        let d = (j+1)+count+10;

        if (renderMethod === "point") {
          drawPoly(Points[a],Points[b],Points[c],Points[a].nz,canvasData);
          drawPoly(Points[d],Points[b],Points[c],Points[a].nz,canvasData);
        } else  if (renderMethod === "flat") {
          drawFilledPoly(Points[a],Points[b],Points[c],Points[a].nz,canvasData);
          drawFilledPoly(Points[d],Points[b],Points[c],Points[a].nz,canvasData);
        } else  if (renderMethod === "texture") {
          drawTexturedPoly(Points[a],Points[b],Points[c],canvasData);
          drawTexturedPoly(Points[d],Points[b],Points[c],canvasData);
        } else  if (renderMethod === "shaded_texture") {
          drawShadedTexturedPoly(Points[a],Points[b],Points[c],canvasData);
          drawShadedTexturedPoly(Points[d],Points[b],Points[c],canvasData);
        } else  if (renderMethod === "envmap") {

          Points[a].tx=Points[a].nx;
          Points[a].ty=Points[a].ny;
          Points[b].tx=Points[b].nx;
          Points[b].ty=Points[b].ny;
          Points[c].tx=Points[c].nx;
          Points[c].ty=Points[c].ny;
          Points[d].tx=Points[d].nx;
          Points[d].ty=Points[d].ny;

          texture=light;

          drawTexturedPoly(Points[a],Points[b],Points[c],canvasData);
          drawTexturedPoly(Points[d],Points[b],Points[c],canvasData);
        }
      }
      count=count+10;
    }

    ctx.putImageData(canvasData, 0, 0);
    
    const outputPoints = [];
    
    for (i=0;i< lathePoints.length; i+=2) {
      outputPoints.push([lathePoints[i], lathePoints[i+1]-200]);
    }
    
    const el = document.querySelector('#output');
    output.textContent = JSON.stringify(outputPoints);
  
  }
  
  function relativeToElement(event, el) {
    const bcr = el.getBoundingClientRect();
    return[event.pageX-bcr.left, event.pageY - bcr.top];
   }
  

   document.body.onmousemove = event =>  {

    let el = document.getElementById("rendercanvas");
    let [x,y] = relativeToElement(event,el);
    if ((y>0) && (y<400) && (x>0) && (x<400)) {
      drawObject(y,x);
    }
    
    el = document.getElementById("editorcanvas");
    [x,y] = relativeToElement(event,el);
    if ((y>0) && (y<400) && (x>0) && (x<800) && (PointBeingMoved>-1)) {
      movePoint(PointBeingMoved,x,y);
    }

  }

  
   document.body.onmousedown = (event) => {
    let el = document.getElementById("editorcanvas");
    let [x,y] = relativeToElement(event,el);
    if ((y>0) && (y<400) && (x>0) && (x<800)) {
      
      if (PointBeingMoved>-1) {
        movePoint(PointBeingMoved,x,y);
      } else if (event.button ===0 ) {
        
          let index = getPointIfHit(x, y);
          
          if (index==-1) {
            addPoint(x,y);
          } else {
            PointBeingMoved = index;
          }
      } else if (event.button === 1) {
          deletePoint(x,y);
      }
    }
  }


  
  document.body.onmouseup = (event) => {
    PointBeingMoved = -1;
  }
